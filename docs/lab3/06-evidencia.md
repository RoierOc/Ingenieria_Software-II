# Fase 6. Evidencia de la ruta A (código funcional)

## Qué exige la ruta A y dónde está

| Requisito de la ruta A | Evidencia |
|---|---|
| Implementar el componente respetando la sub-arquitectura; las carpetas reflejan el estilo | Paquetes "transferencias.dominio", "aplicacion", "puertos.entrada", "puertos.salida" y "adaptadores.salida", más "entrada.consola" ([fase 3](03-componente.md)). "ArquitecturaHexagonalTest" comprueba que el núcleo no importa adaptadores ni la consola. |
| Los seis patrones (mínimo) están en el código y en el informe, con archivo y clase | Tabla de la sección siguiente y [fase 5](05-patrones.md). |
| Sin base de datos real ni UI completa; repositorios en memoria y simulación de servicios externos | "RepositorioEnMemoria", "NotificadorEnMemoria" e "InfraestructuraEnMemoria" en las pruebas; PostgreSQL, SMS, push y antifraude simulados con "System.out". |
| Al menos cinco pruebas unitarias, con una que demuestre Abierto/Cerrado | 30 pruebas. Las de Abierto/Cerrado están en la sección 3. |
| Repositorio con "README.md": cómo ejecutar, estructura y tabla patrón → archivo | [README.md](../../README.md). |
| Historial de commits de todos los integrantes | Pendiente de verificar en el repositorio remoto: cada integrante debe tener commits propios. |

## 1. Patrón → archivo → prueba

Rutas relativas a "src/main/java/co/edu/unal/banco/transferencias/" (las pruebas, a "src/test/java/co/edu/unal/banco/transferencias/aplicacion/").

| Patrón | Archivos | Prueba que lo ejercita |
|---|---|---|
| Builder | "aplicacion/TransaccionServiceBuilder.java" | "TransaccionServiceBuilderTest" (4 pruebas: construye, comisión nueva, sin infraestructura, tipo repetido) |
| Abstract Factory | "aplicacion/InfraestructuraTransferencias.java", "adaptadores/salida/InfraestructuraProduccion.java", "InfraestructuraEnMemoria.java" (pruebas) | "ComisionesYSalidasTest.laFamiliaDeProduccionConectaTodasLasSalidas" y todas las pruebas que usan la familia en memoria |
| Adapter | "adaptadores/salida/NotificadorTransferencia.java" | "NotificacionPushTest" |
| Composite | "adaptadores/salida/CanalNotificacionCompuesto.java", "AuditoriaCompuesta.java" | "NotificacionPushTest", "AntifraudeTest" |
| Strategy | "dominio/PoliticaComision.java", "CalculadoraComision.java", "Comision*.java" | "TransaccionServiceTest", "TransferenciaLlaveTest", "ComisionesYSalidasTest.internacionalCobraTresPorCientoMasVeinticincoMil" |
| Chain of Responsibility | "dominio/ReglaTransferencia.java", "MontoPositivo.java", "TopePorTransferencia.java", "CuentasDistintas.java", "ValidadorTransferencia.java" | "ReglasTransferenciaTest" (3 pruebas) |

## 2. Requisitos → pruebas

| Requisito | Prueba |
|---|---|
| RF-01 Recibir cuentas, monto y tipo | "Main" (ejemplo de consola) y todas las pruebas del servicio |
| RF-02 Validar el monto | "ReglasTransferenciaTest.laCadenaSeDetieneEnLaPrimeraReglaQueFalla" (monto de $6.000.000 rechazado) |
| RF-03 Comisión por tipo | "TransaccionServiceTest" (mismo banco y otro banco), "TransferenciaLlaveTest", "ComisionesYSalidasTest" (internacional), "TransaccionServiceTest.tipoDesconocidoRechazaSinCambiarSaldos" |
| RF-04 Rechazar sin fondos | "TransaccionServiceTest.saldoInsuficienteRechazaSinGuardarNiNotificar" |
| RF-05 Mover fondos | "TransaccionServiceTest.mismoBancoMueveElMontoSinCobrarComision" y "otroBancoDescuentaElMontoMasLaComisionDe7500" |
| RF-06 Registrar una sola vez | "TransaccionServiceTest.transferenciaExitosaGuardaYNotificaUnaSolaVez" |
| RF-07 Comprobante | "ComisionesYSalidasTest.elComprobanteIncluyeCuentasMontoYComision" |
| RF-08 Notificar | "NotificacionPushTest" |
| RF-09 Auditoría | "AntifraudeTest" |
| RF-10 Límite de la cuenta infantil | "CuentaInfantilTest" (6 pruebas) |

Al empezar esta fase, RF-07 y la comisión internacional de RF-03 no tenían prueba, y la fábrica de producción tampoco; se agregaron en "ComisionesYSalidasTest".

## 3. Atributos de calidad

| Atributo | Cómo se comprobó |
|---|---|
| AC-01 Mantenibilidad: una comisión nueva sin cambiar "TransaccionService" ni "CalculadoraComision" | "TransaccionServiceBuilderTest.comisionNuevaSeRegistraSinModificarElServicioNiLaCalculadora" (comisión "PRIORITARIA" definida en la propia prueba) y el escenario 2 de la [fase 7](07-evolucion.md). |
| AC-02 Capacidad de prueba: sin base de datos ni proveedores | Las pruebas del servicio usan "InfraestructuraEnMemoria". |
| AC-03 Independencia de infraestructura: dominio y aplicación no importan adaptadores | "ArquitecturaHexagonalTest" lee los fuentes de "dominio", "puertos", "aplicacion" y "productos" y falla si encuentra una importación de "adaptadores" o de "entrada". Se verificó que detecta una importación prohibida agregada a propósito. El escenario 1 de la fase 7 sustituye un adaptador. |

### Pruebas de Abierto/Cerrado

1. "TransaccionServiceBuilderTest.comisionNuevaSeRegistraSinModificarElServicioNiLaCalculadora": una estrategia nueva entra sin cambiar ninguna clase existente.
2. "ReglasTransferenciaTest.unaReglaNuevaSeAgregaSinModificarLasExistentes": una regla nueva se intercala en la cadena.
3. "EvolucionTest" (fase 7): un canal nuevo, un tipo nuevo y una regla nueva, con las 27 pruebas anteriores sin tocar.

## 4. Ejecución

```sh
./mvnw test        # Windows: .\mvnw.cmd test
```

Resultado esperado: 30 pruebas, 0 fallos (22 heredadas, 5 de esta fase y 3 de la fase 7).

## Límites que se mantienen

- Siguen vigentes los de las fases 1 y 5: "double" para saldos, sin atomicidad ni recuperación si un colaborador falla después de mover los fondos, "AuditoriaTransferencia" alojando el antifraude y tipo de transferencia como "String".
- "ArquitecturaHexagonalTest" revisa importaciones de texto; no detecta dependencias por reflexión ni nombres completos usados sin "import".
