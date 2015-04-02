@echo off
setlocal enableextensions enabledelayedexpansion

for /f "delims=" %%l in ('wmic baseboard get Manufacturer^, serialnumber /format:list') do >nul 2>&1 set "System_%%l"
for /f "delims=" %%l in ('wmic bios get name^, version^, serialnumber /format:list') do >nul 2>&1 set "Bios_%%l"
for /f "delims=" %%l in ('wmic cpu get * /format:list') do >nul 2>&1 set "Cpu_%%l"
for /f "delims=" %%l in ('wmic diskdrive get Name^, Model /format:list') do >nul 2>&1 set "Hdd_%%l"
for /f "delims=" %%l in ('wmic idecontroller get Name^, DeviceID /format:list') do >nul 2>&1 set "Intel_Chipset_%%l"
for /f "delims=" %%l in ('wmic memorychip get SerialNumber /format:list') do >nul 2>&1 set "Memory_%%l"

::Motherboard Manufacturer: 
echo "%System_Manufacturer%"
::Motherboard Serial Number: 
echo "%System_SerialNumber%"

::Bios Name: 
echo "%Bios_Name%"
::Bios Serial Number: 
echo "%Bios_SerialNumber%"
::Bios Version: 
echo "%Bios_Version%"

::Cpu Name: 
echo "%Cpu_Name%"
::Cpu Type: 
echo "%PROCESSOR_ARCHITECTURE%"
::Cpu ID: 
echo "%PROCESSOR_IDENTIFIER%"

::HDD Name: 
echo "%Hdd_Name%"
::HDD Model: 
echo "%Hdd_Model%"

::Intel Chipset Name: 
echo "%Intel_Chipset_Name%"
::Intel Chipset Device ID: 
echo "%Intel_Chipset_DeviceID%"

::Memory Serial Number: 
echo "%Memory_SerialNumber%"

::echo .
::echo .
::pause