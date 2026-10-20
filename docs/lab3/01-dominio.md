# Fase 1. Dominio y problema

## Banco Andino

Banco Andino pertenece al sector financiero. El componente seleccionado procesa transferencias entre cuentas: valida el monto, calcula la comisión según el tipo de operación, mueve los fondos y solicita el registro, el comprobante, la notificación y la auditoría. El problema es mantener esas reglas separadas de los proveedores externos, de modo que cambiar una comisión o un canal de mensajería no obligue a reescribir la transferencia.

El cliente solicita la operación y recibe el comprobante y la notificación. En este laboratorio, una aplicación de consola representa esa solicitud con cuentas de demostración. El banco define topes y comisiones; el registro de transacciones, los proveedores SMS y push y el sistema antifraude son actores externos. Las cuentas se mantienen en memoria. Las clases que representan PostgreSQL, Oracle, SMS, push y antifraude solo imprimen mensajes: no guardan datos ni envían mensajes reales.

El alcance es una demostración académica de transferencias. No incluye autenticación de clientes, apertura de cuentas, compensación entre bancos, tipos de cambio ni despliegue productivo. "OTRO_BANCO" e "INTERNACIONAL" seleccionan comisiones; el movimiento sigue ocurriendo entre objetos locales. Los productos de crédito, CDT y cobros mensuales heredados de la base quedan como código de apoyo, fuera del componente que se desarrollará en este laboratorio.

## Requisitos funcionales

| ID | Requisito | Criterio de aceptación |
|---|---|---|
| RF-01 | Recibir una cuenta de origen, una de destino, un monto y un tipo de transferencia. | La consola ejecuta una operación con las cuentas de demostración configuradas. |
| RF-02 | Validar el monto antes de mover fondos. | Se rechazan montos menores o iguales a cero y mayores a $5.000.000 sin cambiar saldos ni registrar la operación. |
| RF-03 | Calcular la comisión según el tipo de transferencia. | Mismo banco y llave: $0; otro banco: $7.500; internacional: 3 % del monto más $25.000. Un tipo desconocido se rechaza antes del retiro. |
| RF-04 | Rechazar una transferencia cuando el origen no cubra monto y comisión. | Ambos saldos permanecen iguales y no se solicita registro ni notificación. |
| RF-05 | Mover fondos cuando las validaciones se cumplen. | El origen disminuye en monto más comisión y el destino aumenta solo en el monto. |
| RF-06 | Solicitar el registro de una transferencia exitosa. | Se entrega al repositorio una sola operación con origen, destino, monto y comisión. |
| RF-07 | Emitir el comprobante de la transferencia. | La salida contiene cuentas de origen y destino, monto y comisión. |
| RF-08 | Notificar al titular de origen tras una transferencia exitosa. | Se entrega el mensaje con monto y destino a cada canal configurado; la configuración actual incluye SMS y push. |
| RF-09 | Emitir un registro de auditoría. | El registro de auditoría incluye fecha y hora, tipo, cuentas y monto; la configuración actual también reporta la operación al sistema antifraude. |

| RF-10 | Respetar el límite diario de retiros de la cuenta infantil. | Se rechaza un retiro que supere el acumulado diario de $200.000 sin modificar el saldo; la comisión de una transferencia forma parte del retiro. El límite se reinicia al cambiar de día. |

Estos criterios definen el alcance. Las 15 pruebas heredadas cubren transferencias, llave, cuenta infantil, notificaciones y antifraude; se revisará la cobertura completa en la fase 6.

## Atributos de calidad

| ID | Atributo | Criterio medible | Cómo se comprobará |
|---|---|---|---|
| AC-01 | Mantenibilidad | Añadir una comisión requiere una implementación y su registro, sin cambiar "TransaccionService" ni "CalculadoraComision". | Prueba con una política nueva en la fase 6. |
| AC-02 | Capacidad de prueba | Ejecutar los casos de transferencia sin base de datos ni proveedores de mensajería; una transferencia aceptada produce un registro y una llamada al notificador, que puede distribuirla a varios canales. | Pruebas unitarias con dobles en memoria. |
| AC-03 | Independencia de infraestructura | Ninguna clase de dominio o aplicación importa clases de adaptadores; cambiar el proveedor no modifica esas capas. | Revisar importaciones y sustituir un adaptador en un escenario de evolución. |

Los saldos usan "double", como la base del laboratorio anterior. Este modelo no representa contabilidad bancaria de producción. Tampoco hay transacción atómica ni recuperación si un colaborador falla después de mover los fondos; esas garantías quedan fuera de esta entrega y no se consideran satisfechas por la separación en capas.
