unit UnitUtils;

{$mode objfpc}{$H+}

interface

uses
  Windows, Classes, SysUtils;

function GetIniMemParameters: string;

implementation

type
  DWORDLONG = uint64;
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

function GlobalMemoryStatusEx(var Buffer: TMemoryStatusEx): BOOL; stdcall; external 'kernel32' name 'GlobalMemoryStatusEx';

function IsWindows64: boolean;
// https://wiki.lazarus.freepascal.org/Detect_Windows_x32-x64_example
type
  TIsWow64Process = function(Handle: Windows.THandle; var Res: Windows.BOOL): Windows.BOOL; stdcall;
var
  IsWow64Result: Windows.BOOL; // Result from IsWow64Process
  IsWow64Process: TIsWow64Process; // IsWow64Process fn reference
begin
  // Try to load required function from kernel32
  IsWow64Process := TIsWow64Process(Windows.GetProcAddress(Windows.GetModuleHandle('kernel32'), 'IsWow64Process'));
  if Assigned(IsWow64Process) then begin
    // Function is implemented: call it
    IsWow64Result := False;
    if not IsWow64Process(Windows.GetCurrentProcess, IsWow64Result) then begin
      //raise SysUtils.Exception.Create('IsWindows64: bad process handle');
      Result := False;
    end
    else
      Result := IsWow64Result;
  end
  else
    // Function not implemented: can't be running on Wow64
    Result := False;
end;

function MemSize: uint64;
var
  MemoryStatus: TMemoryStatusEx;
begin
  Result := 0;
  MemoryStatus.dwLength := SizeOf(MemoryStatus);
  if GlobalMemoryStatusEx(MemoryStatus) then
    Result := MemoryStatus.ullTotalPhys;
end;

function GetIniMemParameters: string;
var
  MaxHeapSize: uint64;
begin
  MaxHeapSize := ((MemSize div 1024) div 1024) div 2; // half of available physical memory, in megabytes.
  if MaxHeapSize < 256 then
    MaxHeapSize := 256; // default value
  if (not IsWindows64) and (MaxHeapSize > 1500) then
    MaxHeapSize := 1500;
  // Max heap size cannot be less than initial heap size
  if MaxHeapSize > 800 then
    Result := '-Xms800m -Xmx' + IntToStr(MaxHeapSize) + 'm'
  else
    Result := '-Xmx' + IntToStr(MaxHeapSize) + 'm';
end;

end.

