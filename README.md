# Laboratorio L2: SOLID

- Lenguaje: Java 21
- Proyecto: Backend de Banco Andino
- Base: código original del laboratorio, conservado para el diagnóstico.

## Bloque 1 — Diagnóstico

### 1.1 Tabla de hallazgos

| Clase / método | Letra | Evidencia en el código | Consecuencia para el banco o el cliente |
|---|---|---|---|
| "TransaccionService.transferir" ("TransaccionService.java", líneas 7–42) | S | Valida, calcula comisión, mueve dinero, persiste, imprime el comprobante, notifica y audita. | Un cambio de formato del comprobante obliga a editar la clase que mueve el dinero; un error de presentación puede afectar una operación financiera. |
| "TransaccionService.transferir" ("TransaccionService.java", líneas 14–18) | O | El "switch" selecciona la comisión según "tipo". | Agregar un tipo de transferencia exige editar el servicio y puede alterar por accidente una comisión existente. |
| "CDT.retirar" ("CDT.java", líneas 11–18) y "CobroCuotaManejo.cobrarMensual" ("CobroCuotaManejo.java", líneas 6–10) | L | "CDT" hereda de "Cuenta", pero rechaza "retirar" antes del vencimiento; el cobro acepta cualquier "Cuenta". | El proceso mensual puede detenerse al encontrar un CDT. Las cuentas procesadas antes ya fueron cobradas y las siguientes quedan pendientes. |
| "ProductoBancario.java", líneas 1–6, y sus implementaciones | I | La interfaz exige "depositar" y "retirar"; "TarjetaCredito" y "CreditoVivienda" dejan métodos vacíos porque no aplican. | Un cliente puede invocar una operación que el producto no admite y recibir una respuesta silenciosa, sin saber que no ocurrió nada. |
| "TransaccionService.java", líneas 4–5 | D | Construye "OracleRepositorio" y "SmsGateway" con "new". | El servicio queda acoplado a esas clases y no permite sustituirlas por dobles sin modificarlo, lo que dificulta aislar pruebas y cambiar proveedores. |

### 1.2 Experimentos

El CDT. Se incluyó temporalmente el CDT de Ana en la lista de "cobrarMensual". La ejecución cobró primero a Ana y Luis y luego lanzó "UnsupportedOperationException" al intentar retirar del CDT antes de su vencimiento. El saldo del CDT no cambió. El lote no continuó ni revirtió los cobros anteriores. Si el CDT fuera la cuenta número 500.000, las 499.999 anteriores quedarían cobradas y el resto del millón no se procesaría.

La prueba de la comisión. Una prueba temporal llamó a "transferir" por $150.000 a otro banco, capturó la salida y comprobó la comisión de $7.500 y los saldos finales. Si las conexiones fueran reales, la respuesta es no: "TransaccionService" crea directamente esas dependencias y no permite sustituirlas por dobles. Nota: esta ejecución no accedió a Oracle ni envió SMS porque, en este código base, "OracleRepositorio" y "SmsGateway" solo imprimen en consola. La prueba tuvo que capturar la salida para observar la comisión, pues "transferir" no la devuelve.

### 1.3 Medición “antes”

| Métrica | Antes |
|---|---:|
| Líneas del método "transferir" | 36 (líneas 7–42, contando comentarios y líneas vacías) |
| Razones distintas por las que "TransaccionService" podría cambiar | 7: validación, comisión, movimiento, persistencia, comprobante, notificación y auditoría |
| Clases concretas que "TransaccionService" crea con "new" | 2: "OracleRepositorio" y "SmsGateway" |
| Métodos vacíos por “no aplica” | 3: "TarjetaCredito.depositar", "CreditoVivienda.depositar" y "CreditoVivienda.retirar"; "CDT.retirar" también puede lanzar "UnsupportedOperationException", pero por una restricción temporal de negocio |
| ¿Se puede probar sin conexión a Oracle ni envío de SMS? | No: el servicio crea directamente esas dependencias y no permite sustituirlas por dobles. Nota: en esta simulación, ambas clases solo imprimen. |

### 1.4 Diagrama UML del código original

Las relaciones problemáticas de herencia, implementación y dependencia están marcadas en rojo. La dependencia de "TransaccionService" hacia "Cuenta" conserva el color normal.

![UML del código original con relaciones problemáticas en rojo](docs/uml-original.svg)

## Bloque 2 — Refactorización

### Punto de control S

Después del cambio, describan en una frase qué hace "TransaccionService".

"TransaccionService" coordina la ejecución de una transferencia.

¿Aparece la palabra "y"?

No se enumeran responsabilidades adicionales: la responsabilidad del servicio es coordinar la transferencia.

La validación del monto queda en "ValidadorTransferencia", la comisión en "CalculadoraComision", el comprobante en "GeneradorComprobante", el mensaje de notificación en "NotificadorTransferencia" y la auditoría en "RegistroAuditoria". El servicio conserva el orden de ejecución de estas operaciones.

Si el área legal pide cambiar el formato del comprobante, ¿qué archivo tocan?

"GeneradorComprobante.java", que contiene la presentación del comprobante.

### Punto de control O

Si mañana llega un tipo de transferencia nuevo, ¿qué archivos existentes tendrían que modificar? Enumérenlos.

1. "Main.java": registrar la nueva política con el nombre del tipo de transferencia.

Se añade una clase que implemente "PoliticaComision" para definir la comisión nueva. "CalculadoraComision" consulta las políticas registradas y delega el cálculo; ya no contiene el "switch". "TransaccionService" recibe la calculadora configurada desde "Main".

### Punto de control L

¿Su solución detecta el error al compilar o al ejecutar?

Al compilar. "CDT" ya no hereda de "Cuenta": ambos comparten "ProductoConSaldo", que ofrece identificación, saldo y depósito, pero no promete retiros libres. "CobroCuotaManejo" acepta una lista de "Cuenta", por lo que incluir un CDT produce un error de tipos.

¿Por qué es mejor lo primero?

La incompatibilidad se detecta antes de ejecutar el proceso de cobro. Se evita que el lote cobre algunas cuentas y falle después al encontrar el CDT.

Si alguien propone envolver el retiro en un try/catch e ignorar los CDT, ¿por qué eso no resuelve el problema de diseño?

Solo oculta el fallo durante la ejecución. El CDT seguiría presentado como una cuenta sustituible aunque no cumpla el contrato de retiro, y otros clientes de esa jerarquía podrían encontrar el mismo problema.

Se extrajo el estado común a "ProductoConSaldo". "Cuenta" conserva el retiro libre sujeto al saldo disponible; "CDT" conserva su restricción de vencimiento. "Main" declara el CDT con su tipo propio.

### Punto de control I

¿Pudieron lograr que un mismo generador de extractos funcione para cuentas, tarjetas y créditos a la vez?

Sí. "GeneradorExtractos" puede imprimir una lista que contenga "CuentaAhorros", "TarjetaCredito" y "CreditoVivienda".

¿Qué interfaz necesitó para eso, y por qué no necesitó conocer los demás métodos de cada producto?

"ProductoBancario", reducida al método "generarExtracto". El generador solo necesita obtener e imprimir ese texto; no utiliza depósitos, retiros, intereses ni pagos.

Los métodos "calcularIntereses" y "pagarCuota" se agruparon en "ProductoCredito", implementada por la tarjeta y el crédito de vivienda. Se eliminaron "TarjetaCredito.depositar", "CreditoVivienda.depositar" y "CreditoVivienda.retirar", cuyos cuerpos estaban vacíos. "Cuenta" implementa su extracto y "Main" utiliza el generador común con los mismos productos de antes.

### Punto de control D

¿Cuántas clases concretas conoce ahora "TransaccionService"?

Ninguna implementación concreta de la aplicación. Sus seis colaboradores y las cuentas se utilizan mediante interfaces. El servicio recibe los colaboradores por constructor y no crea dependencias con "new".

¿Quién decide si se usa Oracle o si se notifica por SMS?

"Main.java", donde se arma el sistema: selecciona "OracleRepositorio" y entrega un "SmsGateway" a "NotificadorTransferencia". El notificador recibe "CanalNotificacion" y tampoco crea su proveedor.

Vuelvan al experimento 2 del bloque 1: ¿ya es posible esa prueba?

Sí. Se sustituyeron el repositorio y el canal de notificación por dobles en memoria, y el comprobante y la auditoría por implementaciones silenciosas. La prueba verificó una comisión de $7.500, los saldos finales, los datos registrados y el mensaje, sin crear Oracle ni el proveedor SMS. También comprobó que un monto inválido no genera registros ni mensajes.

Se definieron contratos pequeños para validación, cálculo de comisión, persistencia, comprobante, notificación, auditoría, canal de mensajes y operaciones de cuenta. Las implementaciones existentes cumplen esos contratos y "Main" conecta todas las dependencias.

## Bloque 3 — Pruebas unitarias

Se configuraron JUnit y Maven Wrapper. Las pruebas están en "src/test/java" y utilizan cuentas, validación y políticas de comisión reales. "RepositorioEnMemoria" guarda los registros y "NotificadorEnMemoria" conserva las notificaciones; el comprobante y la auditoría utilizan implementaciones silenciosas.

Las cinco pruebas cubren:

1. Transferencia al mismo banco sin comisión y con movimiento exacto del monto.
2. Transferencia a otro banco con comisión de $7.500 descontada del origen.
3. Saldo insuficiente sin registros, notificaciones ni cambios de saldo.
4. Una transferencia exitosa con un registro y una notificación.
5. Tipo desconocido rechazado sin cambios de saldo ni efectos externos.

¿Cuánto tardan en ejecutarse todas sus pruebas?

Las cinco pruebas tardaron 58 ms según el reporte de Maven Surefire de la ejecución en Windows mostrada abajo. Es el tiempo de ejecución de las pruebas; no incluye las descargas, la compilación ni el arranque de Maven.

¿Cuántas líneas de "TransaccionService" tuvieron que cambiar para poder probarla?

Cero. El constructor y las interfaces del control D permiten sustituir sus dependencias por dobles.

¿Qué habría pasado si intentaran estas mismas pruebas en el bloque 1?

El servicio creaba directamente "OracleRepositorio" y "SmsGateway", por lo que no se podían sustituir por dobles. Bajo el supuesto de conexiones reales de la guía, las pruebas accederían a la base de producción y enviarían mensajes al cliente.

![Evidencia de las cinco pruebas ejecutadas en Windows](tests.png)

## Bloque 4 — Negocio pidió cambios

Antes de programar cada requerimiento se revisó el código del commit "bloque-0-codigo-base" para estimar cuántos archivos existentes habría que modificar allí. Luego se implementó sobre el código refactorizado. Los conteos de la tabla del final de esta sección incluyen código de producción y pruebas; no incluyen este README.

### R1 — Transferencias por llave

"ComisionLlave" implementa "PoliticaComision" y devuelve 0. "Main" la registra con el tipo "LLAVE". "TransaccionService" y "CalculadoraComision" no cambiaron: es exactamente el escenario del punto de control O. La búsqueda de la cuenta a partir de la llave no se implementó, como indica el enunciado.

Criterio de aceptación: "TransferenciaLlaveTest" transfiere $50.000 por llave y verifica que el origen pasa de $1.000.000 a $950.000 y que la comisión guardada es 0.

### R2 — Cuenta infantil

"CuentaInfantil" hereda de "Cuenta" y sobrescribe "retirar": lleva el acumulado retirado en el día y rechaza con "IllegalStateException" el retiro que haga superar $200.000, antes de tocar el saldo. El acumulado se reinicia al cambiar de fecha. La fecha se obtiene de un "Clock" que se puede inyectar en las pruebas. Los depósitos se heredan sin límite.

Como es una "Cuenta", funciona sin cambios como origen en "TransaccionService" (que recibe "CuentaTransaccional") y en "CobroCuotaManejo" (que recibe "List<Cuenta>"). Por eso no se modificó ningún archivo existente.

Criterio de aceptación: "CuentaInfantilTest" retira $150.000, intenta retirar $60.000, recibe el rechazo y comprueba que el saldo sigue en $850.000. Otras pruebas cubren el reinicio al día siguiente, los depósitos sin límite, la transferencia desde la cuenta infantil y el cobro de la cuota.

Análisis honesto: "CuentaInfantil" agrega una condición al retiro que "Cuenta" no tiene. Usa la misma excepción que "Saldo insuficiente", así que quien ya maneja ese rechazo no se rompe. Aun así, el diseño no aguantó del todo bien en un punto: "CobroCuotaManejo" cobra con "retirar", de modo que la cuota de manejo cuenta para el límite diario. Si el menor ya retiró $200.000 ese día, el cobro de su cuota se rechaza y el lote se detiene, igual que con una cuenta sin saldo. La comisión de una transferencia también cuenta para el límite. Propuesta: separar el cargo que hace el banco (cuota) del retiro que hace el cliente, por ejemplo con un método de débito propio para cargos en "Cuenta", y hacer que el cobro mensual registre los rechazos en vez de detenerse.

### R3 — Notificaciones push

"PushGateway" implementa "CanalNotificacion" e imprime los mensajes "[PUSH]". "CanalNotificacionCompuesto" también implementa "CanalNotificacion" y reenvía el mensaje a cada canal de su lista. "Main" entrega a "NotificadorTransferencia" un compuesto con "SmsGateway" y "PushGateway". El texto del mensaje sigue en un solo lugar ("NotificadorTransferencia"), y "TransaccionService" sigue llamando una sola vez al notificador, por lo que la prueba 4 del bloque 3 no cambió.

Criterio de aceptación: "NotificacionPushTest" arma el servicio con los dos canales reales, captura la consola y verifica un mensaje "[SMS]" y uno "[PUSH]" para la transferencia. "SalidaConsola" es una utilidad de prueba para capturar la consola; se reutiliza en R4.

### R4 — Sistema antifraude

"SistemaAntifraude" implementa "AuditoriaTransferencia" e imprime el mensaje "[ANTIFRAUDE]". "AuditoriaCompuesta" ejecuta en orden las auditorías de su lista. "Main" entrega una con "RegistroAuditoria" y "SistemaAntifraude". La auditoría actual no cambió. "TransaccionService" registra la auditoría como último paso, así que una transferencia rechazada no llega ni a la auditoría ni al antifraude.

Criterio de aceptación: "AntifraudeTest" verifica un "[AUDITORIA]" y un "[ANTIFRAUDE]" por transferencia exitosa, y ninguno cuando se rechaza por saldo insuficiente.

Análisis honesto: el antifraude reutiliza la interfaz "AuditoriaTransferencia", cuyo nombre es más específico que su papel real: "acción posterior a una transferencia exitosa". Funciona sin modificar el servicio, pero quien lea "Main" puede sorprenderse de ver el antifraude dentro de una "auditoría". Propuesta: renombrar la interfaz a algo como "ObservadorTransferencia". No se hizo para no modificar archivos existentes en este bloque.

### R5 — Migración a PostgreSQL

"PostgresRepositorio" implementa "RepositorioTransacciones" e imprime los mensajes "[POSTGRES]". "Main" lo usa en lugar de "OracleRepositorio", que se conserva sin cambios por si hay que devolverse. Las pruebas no cambiaron porque usan "RepositorioEnMemoria" y nunca conocieron Oracle.

### Tabla del bloque 4

| Req. | Archivos a modificar en el código original (estimado) | Archivos existentes modificados (real) | Archivos nuevos | ¿Se rompió alguna prueba? |
|---|---|---|---|---|
| R1 | 1: "TransaccionService.java" (nuevo "case" en el "switch") | 1: "Main.java" | 2: "ComisionLlave.java", "TransferenciaLlaveTest.java" | No |
| R2 | 0: una clase nueva que herede de "Cuenta" y sobrescriba "retirar" | 0 | 2: "CuentaInfantil.java", "CuentaInfantilTest.java" | No |
| R3 | 1: "TransaccionService.java" (crear "PushGateway" con "new" y llamarlo), más el archivo nuevo del gateway | 1: "Main.java" | 4: "PushGateway.java", "CanalNotificacionCompuesto.java", "NotificacionPushTest.java", "SalidaConsola.java" | No |
| R4 | 1: "TransaccionService.java" (nueva llamada después de la auditoría) | 1: "Main.java" | 3: "SistemaAntifraude.java", "AuditoriaCompuesta.java", "AntifraudeTest.java" | No |
| R5 | 1: "TransaccionService.java" (reemplazar "new OracleRepositorio()"), más el archivo nuevo de PostgreSQL | 1: "Main.java" | 1: "PostgresRepositorio.java" | No |

Al terminar los cinco requerimientos se ejecutan 13 pruebas: las 5 del bloque 3, sin cambios, y 8 nuevas. Todas pasan.

### Análisis

En el código original, R1, R3, R4 y R5 caen en "TransaccionService", la clase que mueve el dinero y que no se podía probar sin Oracle ni SMS. En el código refactorizado, el único archivo existente que cambió fue "Main.java", el punto donde se arma el sistema; la lógica de transferencia no se tocó en ningún requerimiento. El número de archivos modificados es parecido (4 en ambos casos), pero el riesgo no: un error al editar "Main" afecta la configuración, mientras que un error al editar "transferir" puede cobrar mal una transferencia. Además, cada requerimiento quedó con pruebas propias que corren sin infraestructura.

### Salida del programa después del bloque 4

La salida del programa principal cambió a propósito: ahora guarda en PostgreSQL, notifica por SMS y push, y reporta al antifraude después de la auditoría. El resto de la salida es la misma de "salida_original.txt".

```text
[POSTGRES] Conectando a jdbc:postgresql://prod-db:5432/banco...
[POSTGRES] INSERT INTO transacciones VALUES ('001-1', '001-2', 150000.0, 7500.0)
===== BANCO ANDINO - COMPROBANTE =====
Origen: 001-1
Destino: 001-2
Monto:    $150000.0
Comisión: $7500.0
======================================
[SMS] Conectando al proveedor de mensajería...
[SMS] Para Ana: Transferiste $150000.0 a la cuenta 001-2
[PUSH] Conectando al servicio de notificaciones de la app...
[PUSH] Para Ana: Transferiste $150000.0 a la cuenta 001-2
[AUDITORIA] 2026-10-04T13:18:09.161119436 OTRO_BANCO 001-1 -> 001-2 $150000.0
[ANTIFRAUDE] Reportando transacción OTRO_BANCO 001-1 -> 001-2 $150000.0
Cuota de manejo cobrada a 001-1
Cuota de manejo cobrada a 001-2
Tarjeta - deuda: $0.0
Crédito vivienda - pendiente: $1.2E8
```
