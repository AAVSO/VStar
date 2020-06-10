Option Explicit

Function AddTrailingBackslash(S)
  if S <> "" Then
    If Right(S, 1) <> "\" Then S = S & "\"
  End If
  AddTrailingBackslash = S
End Function

Function GetWorkDir
  Dim FileSys
  Set FileSys = CreateObject("Scripting.FileSystemObject")
  GetWorkDir = AddTrailingBackslash(FileSys.GetParentFolderName(WScript.ScriptFullName))
End Function

Sub Main
  Dim Shell, DesktopPath, Link, WorkDir
  Set Shell = CreateObject("WScript.Shell")
  DesktopPath = AddTrailingBackslash(Shell.SpecialFolders("Desktop"))
  Set Link = Shell.CreateShortcut(DesktopPath & "AAVSO VStar.lnk")
  WorkDir = GetWorkDir 
  Link.TargetPath = GetWorkDir & "VStar.exe"
  Link.WorkingDirectory = GetWorkDir
  Link.Save
End Sub

Main



