# Fase 7. Prueba de evolución («¿Qué pasa si…?»)

Cada escenario se implementó sobre el código de la fase 6 (46 archivos de producción y 27 pruebas) y se midió con una comparación de archivos antes y después. Las pruebas están en "EvolucionTest"; ninguna prueba anterior se modificó. Rutas relativas a "src/main/java/co/edu/unal/banco/".

## Escenario 1. Llega un nuevo canal de mensajería (WhatsApp)

El banco quiere avisar también por WhatsApp.

| | Clases |
|---|---|
| **Se crea (1)** | "transferencias/adaptadores/salida/WhatsAppGateway" (implementa "CanalNotificacion") |
| **Se modifica (1)** | "InfraestructuraProduccion": una hoja más en la lista de "CanalNotificacionCompuesto" |
| **No se tocan (45)** | "TransaccionService", "TransaccionServiceBuilder", "NotificadorTransferencia", "CanalNotificacionCompuesto", "CanalNotificacion" (puerto), "SmsGateway", "PushGateway", "InfraestructuraTransferencias", las pruebas… |

Por qué aguanta: el servicio solo conoce "NotificacionTransferencia"; el adaptador solo conoce "CanalNotificacion"; el compuesto trata una hoja nueva como cualquier otra (Composite, **O** y **D**). Prueba: "escenario1_whatsAppRecibeLaNotificacionJuntoConSmsYPush".

Efecto secundario real: la salida de "Main" ahora incluye dos líneas "[WHATSAPP]". La fase 5 afirma que la salida con "InfraestructuraProduccion" es idéntica a la anterior; esa frase ya no es cierta después de este cambio.

## Escenario 2. Aparece un nuevo tipo de transferencia: pago de servicios (comisión fija de $1.500)

| | Clases |
|---|---|
| **Se crea (1)** | "transferencias/dominio/ComisionPagoServicios" (implementa "PoliticaComision") |
| **Se modifica (1)** | "entrada/consola/Main": registra "PAGO_SERVICIOS" en el builder (una línea y su importación) |
| **No se tocan (45)** | "CalculadoraComision", "TransaccionService", "TransaccionServiceBuilder", las otras cuatro comisiones, la cadena de reglas, todos los adaptadores… |

Por qué aguanta: la comisión es una estrategia y el builder las registra una a una (Strategy y Builder, **O**). Prueba: "escenario2_pagoDeServiciosCobraUnaComisionFijaDe1500" (un pago de $184.300 deja el origen en $814.200).

## Escenario 3. Una nueva regla de validación: monto mínimo de $1.000

| | Clases |
|---|---|
| **Se crea (1)** | "transferencias/dominio/MontoMinimo" (nuevo eslabón de "ReglaTransferencia") |
| **Se modifica (1)** | "ValidadorTransferencia": la agrega a la cadena estándar |
| **No se tocan (45)** | "ReglaTransferencia", "MontoPositivo", "TopePorTransferencia", "CuentasDistintas", "ValidacionTransferencia", "TransaccionService", "TransaccionServiceBuilder"… |

Por qué aguanta: cada regla comprueba una sola condición y la cadena se arma aparte (Chain of Responsibility, **S** y **O**). Prueba: "escenario3_unMontoInferiorAlMinimoSeRechazaSinMoverDinero" (rechazado sin cambiar saldos ni registrar).

Alternativa sin modificar el dominio: pasar la cadena desde "Main" con "TransaccionServiceBuilder.validarCon(...)". Se eligió la cadena estándar porque el mínimo es una regla del banco para todas las transferencias.

## Resumen

| Escenario | Clases nuevas | Existentes modificadas | Intactas (de 46) | ¿Se rompió una prueba anterior? |
|---|:-:|:-:|:-:|:-:|
| 1. Canal WhatsApp | 1 | 1 ("InfraestructuraProduccion") | 45 | No |
| 2. Pago de servicios | 1 | 1 ("Main") | 45 | No |
| 3. Monto mínimo | 1 | 1 ("ValidadorTransferencia") | 45 | No |
| **Los tres** | **3** | **3** | **43** | **No** (27 anteriores y 3 nuevas pasan) |

"TransaccionService", "CalculadoraComision" y "TransaccionServiceBuilder" quedaron intactos en los tres. Lo que cambió fue siempre un sitio de armado ("Main", "InfraestructuraProduccion" o la cadena estándar), no la lógica que mueve el dinero.

## Un cambio que el diseño no absorbe bien

Una regla que dependa del tipo de transferencia (por ejemplo, un tope menor para "INTERNACIONAL") no cabe como clase nueva: "ValidacionTransferencia.validar" y "ReglaTransferencia.comprobar" reciben origen, destino y monto, pero no el tipo. Habría que cambiar esa firma en 7 archivos de producción ("ValidacionTransferencia", "ReglaTransferencia", "MontoPositivo", "TopePorTransferencia", "CuentasDistintas", "ValidadorTransferencia" y "TransaccionService") y en "ReglasTransferenciaTest", que define reglas propias. Es una estimación por búsqueda de referencias, no una implementación. Sigue la misma lección del tipo como "String" de la fase 5: lo que no es una clase propia no puede crecer sin tocar a quienes lo reciben.
