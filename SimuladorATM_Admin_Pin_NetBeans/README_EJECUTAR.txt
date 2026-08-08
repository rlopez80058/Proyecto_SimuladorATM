SIMULADOR ATM - NETBEANS
=========================

Proyecto Java Swing para NetBeans con:
- Simulador de cajero automático ATM.
- Inicio de sesión con tarjeta y PIN.
- Consulta de saldo, retiro, depósito, transferencia e historial.
- Módulo ADMIN USUARIOS protegido por PIN.
- Crear, editar, eliminar, activar/inactivar y desbloquear usuarios.

CÓMO ABRIR EN NETBEANS
----------------------
1. Descomprima el ZIP.
2. Abra NetBeans.
3. File > Open Project.
4. Seleccione la carpeta SimuladorATM_Admin_Pin_NetBeans.
5. Ejecute Source Packages > cajeroatm > ATMApp.java > Run File.

DATOS DE PRUEBA ATM
-------------------
Tarjeta: 1111222233334444
PIN: 1234

Tarjeta: 5555666677778888
PIN: 4321

Cuentas para transferencias:
1001, 1002, 2001

ACCESO AL MÓDULO ADMIN
----------------------
Botón: ADMIN USUARIOS
PIN administrador: 1234

El módulo admin permite crear nuevos usuarios. Los usuarios creados pueden iniciar sesión en el ATM con su tarjeta y PIN.

EJECUTAR JAR
------------
También puede ejecutar el archivo:
SimuladorATM_Admin_Usuarios.jar

O usar ejecutar.bat en Windows si Java está configurado.
