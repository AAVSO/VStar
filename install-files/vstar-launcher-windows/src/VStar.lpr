program VStar;

{$R *.res}
{$R VStarIcon.rc}

uses
  Windows, SysUtils, Classes, IniFiles, Process;

var
  AProcess: TProcess;
  Ini: TMemIniFile;
  IniParam: string;
  IniParamList: TStringList;
  VStarHome: string;
  ErrorString: string;
  JavaPath: string;
  JVMexecutable: string;
  JVMparameters: string;
  ShowParameters: Boolean;
  I: Integer;

// This program runs VStar Java application like existing VStar.bat does.
// It seems VSTAR_HOME environment variable is not used by VStar itself
// yet it is added to make the launcher behave like VStar.bat.
// VStar.ini file is used to specify additional JVM parameters
// (memory allocation options, etc.)
// javaw.exe is used instead of java.exe to prevent creation of a console
// window.

begin
  ErrorString := '';
  JavaPath := '';
  JVMexecutable := 'javaw.exe';
  JVMparameters := '';
  try
    Ini := TMemIniFile.Create(ChangeFileExt(ParamStr(0), '.INI'));
    try
      IniParam := Trim(Ini.ReadString('Settings', 'Parameters', ''));
      ShowParameters := Ini.ReadBool('Settings', 'ShowParameters', False);
      VStarHome := Trim(Ini.ReadString('Settings', 'VSTAR_HOME', ''));
      JavaPath := Trim(Ini.ReadString('Settings', 'JavaPath', ''));
    finally
      FreeAndNil(Ini);
    end;
    if JavaPath <> '' then
      JVMexecutable := IncludeTrailingPathDelimiter(JavaPath) + JVMexecutable;
    AProcess := TProcess.Create(nil);
    try
      // Copy environment plus VSTAR_HOME
      for I := 1 to GetEnvironmentVariableCount do
        AProcess.Environment.Add(GetEnvironmentString(I));
      if VStarHome = '' then
        VStarHome := GetEnvironmentVariable('VSTAR_HOME');
      if VStarHome = '' then begin
        VStarHome := ExtractFilePath(ParamStr(0));
        AProcess.Environment.Add('VSTAR_HOME=' + VStarHome);
      end;
      VStarHome := IncludeTrailingPathDelimiter(VStarHome);
      // JVM command-line arguments
      AProcess.Parameters.Add('-splash:"' + VStarHome + 'extlib\vstaricon.png"');
      if IniParam <> '' then begin
        IniParamList := TStringList.Create;
        try
          IniParamList.QuoteChar := '"';
          IniParamList.Delimiter := ' ';
          IniParamList.DelimitedText := IniParam;
          for I := 0 to IniParamList.Count - 1 do
            AProcess.Parameters.Add(IniParamList[I]);
        finally
          FreeAndNil(IniParamList);
        end;
      end;
      AProcess.Parameters.Add('-jar "' + VStarHome + 'dist\vstar.jar"');
      // Any additional parameters passed through command-line
      for I := 1 to ParamCount do
        AProcess.Parameters.Add(ParamStr(I));
      AProcess.Executable := JVMexecutable;
      JVMparameters := AProcess.Parameters.Text;
      // Some debug optiion
      if ShowParameters then begin
        Windows.MessageBox(0,
          PChar(JVMparameters),
          PChar('Parameters of ' + JVMexecutable), MB_OK or MB_ICONINFORMATION);
      end;
      // Running VStar Java application
      AProcess.Execute;
    finally
      FreeAndNil(AProcess);
    end;
  except
    on E: Exception do begin
      ErrorString := 'Exception: ' + E.ClassName + ^M^J + 'Error: ' + E.Message;
    end;
  end;
  if ErrorString <> '' then begin
    ErrorString := ErrorString + ^M^J^M^J;
    ErrorString := ErrorString + 'Executable: ' + JVMexecutable + ^M^J;
    ErrorString := ErrorString + 'Parameters:'^M^J;
    ErrorString := ErrorString + JVMparameters + ^M^J;
    Windows.MessageBox(0,
      PChar('Cannot launch VStar.'^M^J + ErrorString),
      'VStar Launcher', MB_OK or MB_ICONERROR);
  end;
end.

