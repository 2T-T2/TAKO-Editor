@echo off
cd %~dp0
set PATH=.\min-jre\bin;%PATH%
start javaw -jar TAKO_Editor.jar %1
