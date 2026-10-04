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
