# Proyecto 2 Parte 2 - Sistemas Operativos

## ¿Cómo correrlo?

Para ejecutar el programa es necesario tener instalado el compilador de Java junto con Java
Es necesario ejecutar los siguientes comandos desde la carpeta out:

> Es necesario que te ubiques en la carpeta out

~~~bash
# Windows
cd Karel\out

# Linux
cd Karel/out
~~~

Para compilar el programa en:
```bash
# Para Windows
javac -cp ".;../lib/KarelJRobot.jar" -d . ../src/*.java

# Para Linux
javac -cp ".:../lib/KarelJRobot.jar" -d . ../src/*.java
```

Para ejecutar el programa
```bash
# Para Windows
java -cp ".;../lib/KarelJRobot.jar" Main > .log

# Para Linux
java -cp ".:../lib/KarelJRobot.jar" Main > .log
```
