@echo off
echo === SIMULACION PARE Y SIGA - SISTEMAS OPERATIVOS ===
echo Compilando SimulacionPareSigaFinal.java...
javac -cp ".;KarelJRobot.jar" SimulacionPareSigaFinal.java

if %errorlevel% equ 0 (
    echo Compilacion exitosa! Ejecutando simulacion...
    java -cp ".;KarelJRobot.jar" SimulacionPareSigaFinal
) else (
    echo Error en la compilacion.
    pause
)