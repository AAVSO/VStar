;VStar InnoSetup Script

#define TheGroupName "VStar"
#define TheAppName "AAVSO VStar"
#define TheAppDebugName "VStar Debug Mode"
; Normally, TheAppVersion defined via ISCC.exe command-line parameter (see build-win-installer.xml)
#ifndef TheAppVersion
  #define TheAppVersion "Unversioned"
#endif
#define TheAppPublisher "AAVSO"
#define TheAppURL "https://aavso.org/vstar"
#define TheAppExeName "VStar.exe"
#define TheAppCnfName "VStar.ini"
#define TheAppConfig "VStar Launcher Configuration"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{1860E797-A226-48ED-891A-2B3A0960A83D}
AppName={#TheAppName}
AppVersion={#TheAppVersion}
AppPublisher={#TheAppPublisher}
AppPublisherURL={#TheAppURL}
AppSupportURL={#TheAppURL}
AppUpdatesURL={#TheAppURL}
DefaultDirName={%HOMEDRIVE}{%HOMEPATH}\vstar
DefaultGroupName={#TheGroupName}
;DisableProgramGroupPage=yes
DisableWelcomePage=no
WizardImageFile=tenstar_artist_conception1.bmp
WizardSmallImageFile=aavso.bmp
OutputBaseFilename=VStarWinSetup-{#TheAppVersion}
OutputDir=..\
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: ; Components: core

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "vstar\{#TheAppExeName}"; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core
Source: "vstar\{#TheAppCnfName}"; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core
; We should copy VStar.bat to destination (it will be rewritten in 'PostInstall') to make it uninstallable.
Source: "vstar\VStar.bat"       ; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core
Source: "vstar\VeLa.bat"        ; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core
Source: "vstar\ChangeLog.txt"   ; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core
; Helper Java class for VStar.exe 
Source: "vstar\JavaOsArch.class"; DestDir: "{app}"            ; Flags: ignoreversion                                ; Components: core

Source: "vstar\data\*"          ; DestDir: "{app}\data"       ; Flags: ignoreversion recursesubdirs createallsubdirs; Components: core
Source: "vstar\dist\*"          ; DestDir: "{app}\dist"       ; Flags: ignoreversion recursesubdirs createallsubdirs; Components: core
Source: "vstar\doc\*"           ; DestDir: "{app}\doc"        ; Flags: ignoreversion recursesubdirs createallsubdirs; Components: core
Source: "vstar\extlib\*"        ; DestDir: "{app}\extlib"     ; Flags: ignoreversion recursesubdirs createallsubdirs; Components: core
Source: "vstar\plugin-dev\*"    ; DestDir: "{app}\plugin-dev" ; Flags: ignoreversion recursesubdirs createallsubdirs; Components: core
;;
Source: "vstar_plugins\*"       ; DestDir: "{%USERPROFILE}\vstar_plugins"    ; Flags: ignoreversion; Components: plugins
Source: "vstar_plugin_libs\*"   ; DestDir: "{%USERPROFILE}\vstar_plugin_libs"; Flags: ignoreversion; Components: plugins
;;
Source: "vstar\vstar.properties"; DestDir: "{%USERPROFILE}\.vstar"           ; Flags: ignoreversion; Components: plugins
Source: "vstar\PyMicroService\*"; DestDir: "{app}\PyMicroService"            ; Flags: ignoreversion; Components: plugins

[UninstallDelete]
Type: filesandordirs; Name: "{app}\PyMicroService"
Type: filesandordirs; Name: "{%USERPROFILE}\vstar_log"

[Icons]
Name: "{group}\{#TheAppName}"; Filename: "{app}\{#TheAppExeName}"
Name: "{group}\{#TheAppDebugName}"; Filename: "{app}\{#TheAppExeName}"; Parameters: "//DEBUG"
Name: "{group}\Restore Default Memory Options"; Filename: "{app}\{#TheAppExeName}"; Parameters: "//RESTORE"
Name: "{group}\{#TheAppConfig}"; Filename: "{app}\{#TheAppCnfName}"
Name: "{userdesktop}\{#TheAppName}" ; Filename: "{app}\{#TheAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#TheAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(TheAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
Filename: "{#TheAppURL}"; Description: "Visit Application Website"; Flags: postinstall shellexec skipifsilent

[Types]
Name: "full";         Description: "Full Installation"
Name: "custom";       Description: "Custom Installation"; Flags: iscustom

[Components]
Name: "core";       Description: "VStar core"    ; Types: custom full; Flags: fixed
Name: "plugins";    Description: "VStar plugins" ; Types: custom full

[Code]

var
  JavaFound: Boolean;

type
  DWORDLONG = Int64; // Should be unsigned, currently not available
  TMemoryStatusEx = record
    dwLength: DWORD;
    dwMemoryLoad: DWORD;
    ullTotalPhys: DWORDLONG;
    ullAvailPhys: DWORDLONG;
    ullTotalPageFile: DWORDLONG;
    ullAvailPageFile: DWORDLONG;
    ullTotalVirtual: DWORDLONG;
    ullAvailVirtual: DWORDLONG;
    ullAvailExtendedVirtual: DWORDLONG;
  end;

function GlobalMemoryStatusEx(var lpBuffer: TMemoryStatusEx): BOOL;
  external 'GlobalMemoryStatusEx@kernel32.dll stdcall';

function MemSize: Int64;
var
  MemoryStatus: TMemoryStatusEx;
begin
  Result := 0;
  MemoryStatus.dwLength := SizeOf(MemoryStatus);
  if GlobalMemoryStatusEx(MemoryStatus) then
    Result := MemoryStatus.ullTotalPhys;
end;

function DateTime: String;
begin
  Result := GetDateTimeString('yyyy/mm/dd"T"hh:nn:ss', '-', ':');
end;

function InitIniMemParameters(Is64bit: Boolean): string;
var
  MaxHeapSize: Int64;
begin
  MaxHeapSize := ((MemSize div 1024) div 1024) div 2; // half of available physical memory, in megabytes.
  if MaxHeapSize < 256 then
    MaxHeapSize := 256; // default value
  if (not Is64bit) and (MaxHeapSize > 1500) then
    MaxHeapSize := 1500;
  // Max heap size cannot be less than initial heap size
  if MaxHeapSize > 800 then
    Result := '-Xms800m -Xmx' + Int64toStr(MaxHeapSize) + 'm'
  else
    Result := '-Xmx' + Int64toStr(MaxHeapSize) + 'm';
end;

function GetIniDescription(Param: string): string;
begin
  Result := 'VStar.exe configuration file created at ' + DateTime;
end;

procedure MakeVStarIni(const IniMemParameters: string);
var
  S: String;
begin
  S := 
    '[Settings]'#13#10 +
    'Description=VStar.exe configuration file created at ' + DateTime + #13#10 +
    ';Additional JVM parameters'#13#10 +
    'Parameters=' + IniMemParameters + #13#10 +
    ';Set ShowParameters=1 to view parameters to be passed to Java VM'#13#10 +
    'ShowParameters=0'#13#10 +
    ';VSTAR_HOME parameter overrides environment variable VSTAR_HOME'#13#10 +
    'VSTAR_HOME='#13#10 +
    ';JavaPath'#13#10 +
    'JavaPath='#13#10;
  if not SaveStringsToFile(ExpandConstant('{app}') + '\' + '{#TheAppCnfName}', [S], False) then begin
    MsgBox('Cannot create ' + '{#TheAppCnfName}', mbError, MB_OK);
  end;
end;

procedure MakeBatLauncher(const IniMemParameters: string);
var
  S: String;
begin
  S := 
    '@echo off'#13#10 +
    ''#13#10 +
    ':: An alternative VStar launcher created at ' + DateTime + #13#10 +
    ''#13#10 +
    ':: VSTAR_HOME needs to be set to the VStar root directory,'#13#10 +
    ':: e.g. set VSTAR_HOME=C:\vstar'#13#10 +
    ':: If not set, the script assumes the current directory is the'#13#10 +
    ':: directory that the script is running from.'#13#10 +
    ''#13#10 +
    'title VStar'#13#10 +
    ''#13#10 +
    'if not "%VSTAR_HOME%" == "" goto :RUN'#13#10 +
    ''#13#10 +
    'set VSTAR_HOME=%~dp0'#13#10 +
    ''#13#10 +
    ':RUN'#13#10 +
    'java -splash:"%VSTAR_HOME%\extlib\vstaricon.png" ' + IniMemParameters + ' -jar "%VSTAR_HOME%\dist\vstar.jar" %*'#13#10 +
    'if ERRORLEVEL 1 goto :ERROR'#13#10 +
    'goto :EOF'#13#10 +
    ''#13#10 +
    ':ERROR'#13#10 +
    'echo *** Nonzero exit code: possible ERROR running VStar'#13#10 +
    'pause'#13#10;
  if not SaveStringsToFile(ExpandConstant('{app}') + '\VStar.bat', [S], False) then begin
    MsgBox('Cannot create VStar.bat', mbError, MB_OK);
  end;
end;

procedure CurStepChanged(CurStep: TSetupStep);
var
  IniMemParameters: string;
  ResultCode: Integer;
  Is64Bit: Boolean;
  JavaArchMessage: string;
begin
  if CurStep = ssPostInstall then begin
    //Is64Bit = IsWin64;

    if JavaFound then begin
      try
        JavaFound := Exec('java.exe', 'JavaOsArch', ExpandConstant('{app}'), SW_HIDE, ewWaitUntilTerminated, ResultCode);
      except
        JavaFound := False;
        ResultCode := 1;
      end;
    end;
    
    Is64Bit := JavaFound and (ResultCode = 0);

    if Is64Bit then
      JavaArchMessage := 'Java 64-bit detected.'#13#10#13#10
    else
      JavaArchMessage := '';

    IniMemParameters := InitIniMemParameters(Is64Bit);
    MakeVStarIni(IniMemParameters);
    MakeBatLauncher(IniMemParameters);
    MsgBox(JavaArchMessage + 'Java memory options have been set to'#13#10 + 
            IniMemParameters + #13#10 +
           'To modify them, edit ' + #13#10 + 
           '"' + ExpandConstant('{app}') + '\' + '{#TheAppCnfName}"'#13#10 + 
           'file.', 
           mbInformation, MB_OK);
  end;
end;

procedure InitializeWizard;
var
  RichViewer: TRichEditViewer;
  Message: String;
  //JavaVer: string;
  ResultCode: Integer;
begin
  RichViewer := TRichEditViewer.Create(WizardForm);
  RichViewer.Left := WizardForm.WelcomeLabel2.Left;
  RichViewer.Top := WizardForm.WelcomeLabel2.Top;
  RichViewer.Width := WizardForm.WelcomeLabel2.Width;
  RichViewer.Height := WizardForm.WelcomeLabel2.Height;
  RichViewer.Parent := WizardForm.WelcomeLabel2.Parent;
  RichViewer.BorderStyle := bsNone;
  RichViewer.TabStop := False;
  RichViewer.ReadOnly := True;
  WizardForm.WelcomeLabel2.Visible := False;
  Message := 
    '{\rtf1 This will install {#SetupSetting("AppName")} ' + 
    'version {#SetupSetting("AppVersion")} on your computer.\par\par ' +
    'Click Next to continue or Cancel to exit Setup.';

  // Cannot read HKLM if PrivilegesRequired=lowest.
  //JavaVer := '';
  //if not RegQueryStringValue(HKEY_LOCAL_MACHINE, 'SOFTWARE\JavaSoft\Java Runtime Environment',
  //  'CurrentVersion', JavaVer)
  //then
  //  JavaVer := '';
  
  // Instead checking The Registry, testing for the JRE executable.
  try
    JavaFound := Exec('javaw.exe', '', '', SW_HIDE, ewNoWait, ResultCode);
  except
    JavaFound := False;
  end;
  if not JavaFound then begin
    Message := Message + 
      '\par\par Test for "javaw.exe" failed!\par It seems that Java is not installed on your machine.\par ' + 
      'You can download and install Java from: \par' + 
      '{\field{\*\fldinst{HYPERLINK "https://openjdk.org/"}}{\fldrslt{\ul\cf1 https://openjdk.org/}}}';
  end;
  Message := Message + '}';
  RichViewer.RTFText := Message;
end;

