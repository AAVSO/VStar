;VStar InnoSetup Script

#define TheAppName "AAVSO VStar"
; Normally, TheAppVersion defined via ISCC.exe command-line parameter (see build-win-installer.xml)
#ifndef TheAppVersion
  #define TheAppVersion "Unversioned"
#endif
#define TheAppPublisher "AAVSO"
#define TheAppURL "https://aavso.org/vstar"
#define TheAppExeName "VStar.exe"

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
DisableProgramGroupPage=yes
DisableWelcomePage=no
WizardImageFile=tenstar_artist_conception1.bmp
WizardSmallImageFile=aavso.bmp
OutputBaseFilename=VStarSetup_{#TheAppVersion}
OutputDir=..\
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags:

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "vstar\VStar.exe"     ; DestDir: "{app}"            ; Flags: ignoreversion
Source: "vstar\VStar.ini"     ; DestDir: "{app}"            ; Flags: ignoreversion
Source: "vstar\VStar.bat"     ; DestDir: "{app}"            ; Flags: ignoreversion
Source: "vstar\VeLa.bat"      ; DestDir: "{app}"            ; Flags: ignoreversion
Source: "vstar\ChangeLog.txt" ; DestDir: "{app}"            ; Flags: ignoreversion
Source: "vstar\data\*"        ; DestDir: "{app}\data"       ; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "vstar\dist\*"        ; DestDir: "{app}\dist"       ; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "vstar\doc\*"         ; DestDir: "{app}\doc"        ; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "vstar\extlib\*"      ; DestDir: "{app}\extlib"     ; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "vstar\plugin-dev\*"  ; DestDir: "{app}\plugin-dev" ; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{userprograms}\{#TheAppName}"; Filename: "{app}\{#TheAppExeName}"
Name: "{userdesktop}\{#TheAppName}" ; Filename: "{app}\{#TheAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#TheAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(TheAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent
Filename: "{#TheAppURL}"; Description: "Visit Application Website"; Flags: postinstall shellexec skipifsilent

[Code]

procedure InitializeWizard;
var
  RichViewer: TRichEditViewer;
  Message: String;
  //JavaVer: string;
  JavaFound: Boolean;
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
      '\par\par Test for "javaw.exe" failed!\par It seems there is no Java Runtime Environment (JRE) installed on your machine.\par ' + 
      'You can download JRE installer from the Java download site \par' + 
      '{\field{\*\fldinst{HYPERLINK "https://www.java.com/download/"}}{\fldrslt{\ul\cf1 https://www.java.com/download/}}}';
  end;
  Message := Message + '}';
  RichViewer.RTFText := Message;
end;

