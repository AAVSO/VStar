program VStar;

{$mode objfpc}{$H+}

{$R *.res}
{$R VStarIcon.rc}

uses
  Windows, SysUtils, Classes, IniFiles, Process, UnitUtils;

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
  DebugMode: Boolean;
  RestoreINIandExit: Boolean;
  IsJava64var: Boolean;
  Java64Message: string;
  S: string;
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
  DebugMode := False;
  RestoreINIandExit := False;
  // Check command line for the special VStar.exe parameters.
  for I := 1 to ParamCount do begin
    S := ParamStr(I);
    if Copy(S, 1, 2) = '//' then begin
      // A special VStar.exe parameter found.
      if AnsiUpperCase(S) = '//RESTORE' then begin
        // Restore default JRE prarameters
        RestoreINIandExit := True;
      end
      else
      if AnsiUpperCase(S) = '//DEBUG' then begin
        // Debug mode: create console, use console-mode java.exe,
        // wait on exit to see possible error output.
        DebugMode := True;
        JVMexecutable := 'java.exe';
        AllocConsole;
        IsConsole := True;
        SysInitStdIO;
      end;
    end;
  end;
  try
    Ini := TMemIniFile.Create(ChangeFileExt(ParamStr(0), '.ini'));
    try
      JavaPath := Trim(Ini.ReadString('Settings', 'JavaPath', ''));
      if RestoreINIandExit then begin
        try
          IsJava64var := IsJava64(JavaPath);
        except
           IsJava64var := False;
        end;
        if IsJava64var then
          Java64Message := 'Java 64-bit detected.'^M^J^M^J
        else
          Java64Message := 'Cannot detect Java 64-bit. Assuming 32-bit architecture.'^M^J^M^J;
        S := GetIniMemParameters(IsJava64var);
        if Windows.MessageBox(0,
          PChar(Java64Message + 'Java memory options will be set to'^M^J + S),
          PChar(Ini.FileName), MB_OKCANCEL or MB_ICONQUESTION or MB_SYSTEMMODAL) = IDOK then
        begin
          Ini.WriteString('Settings', 'Parameters', S);
          Ini.UpdateFile;
          S := Ini.ReadString('Settings', 'Parameters', '');
          Windows.MessageBox(0,
            PChar('Java memory options have been set to'^M^J + S),
            PChar(Ini.FileName), MB_OK or MB_ICONINFORMATION or MB_SYSTEMMODAL);
        end;
        Exit;
      end;
      IniParam := Trim(Ini.ReadString('Settings', 'Parameters', ''));
      ShowParameters := Ini.ReadBool('Settings', 'ShowParameters', False);
      VStarHome := Trim(Ini.ReadString('Settings', 'VSTAR_HOME', ''));
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
      // but VStar.exe special parameters!
      for I := 1 to ParamCount do begin
        S := ParamStr(I);
        if Copy(S, 1, 2) = '//' then begin
          // special VStar.exe parameter. do not pass it to Java!
        end
        else
          AProcess.Parameters.Add(ParamStr(I));
      end;
      AProcess.Executable := JVMexecutable;
      JVMparameters := AProcess.Parameters.Text;
      // Some debug optiion
      if ShowParameters then begin
        Windows.MessageBox(0,
          PChar(JVMparameters),
          PChar('Parameters of ' + JVMexecutable), MB_OK or MB_ICONINFORMATION or MB_SYSTEMMODAL);
      end;
      // Running VStar Java application
      if DebugMode then begin
        AProcess.Options := AProcess.Options + [poWaitOnExit];
        WriteLn('Launching ' + JVMexecutable);
        WriteLn('With parameters:');
        WriteLn(JVMparameters);
      end;
      AProcess.Execute;
      if DebugMode then begin
          Windows.MessageBox(0,
          PChar('Process terminated.'),
          PChar(JVMexecutable), MB_OK or MB_ICONINFORMATION or MB_SYSTEMMODAL);
       end;
    finally
      FreeAndNil(AProcess);
    end;
  except
    on E: Exception do begin
      ErrorString := 'Exception: ' + E.ClassName + ^M^J + 'Error: ' + E.Message;
    end;
  end;
  if ErrorString <> '' then begin
    if not RestoreINIandExit then begin
      ErrorString := ErrorString + ^M^J^M^J;
      ErrorString := ErrorString + 'Executable: ' + JVMexecutable + ^M^J;
      ErrorString := ErrorString + 'Parameters:'^M^J;
      ErrorString := ErrorString + JVMparameters + ^M^J;
      Windows.MessageBox(0,
        PChar('Cannot launch VStar.'^M^J + ErrorString),
        'VStar Launcher', MB_OK or MB_ICONERROR or MB_SYSTEMMODAL);
    end
    else
      Windows.MessageBox(0,
        PChar(ErrorString),
        'VStar Launcher', MB_OK or MB_ICONERROR or MB_SYSTEMMODAL);
  end;
end.

