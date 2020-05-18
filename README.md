# Trade-FDV
S.O:  **Android**
App para FDV en android
## Lista De Versiones
- **1.2** Tomada como base en repositorio, archivos fuentes de equipo personal de Carlos E. Mera.
- **1.3** se encuentra en Google Play como beta, pero no ha sido liberada
- **1.4** Actualización del gestor de notificaciones GCM a FCM::
	- Actualizado SDK **27** a **29**.
	- Actualizado buildToolsVersion **28.0.0 rc1** a **29.0.2**.
	- Actualizadas dependencias a las versiones recientes disponibles.
	- Se modifica **minSdkVersion** de **15** a **16** ya que FCM soporta, como mínimo, Android 4.1 (SDK 16).
	- Se remplaza **loadData()** para el View de los mensajes del Chat por **loadDataWithBaseURL()**.
	Se presentaba errores de visualización en algunos dispositivos, como es el caso de los Huawei.
- **1.5** Correcciónes 16/10/2019:    
    - Se corrige problema en el inicio de ruta.
    - Corrección en la selección en la lista de puntos de venta para el agendamiento.
- **1.6** Correcciones
    - **13/01/2020:**
        - Se corrige visualización de usuarios PUNTOS DE VENTA para el chat, se modifica el perfil por 8 en lugar de 7.
        - Se elimina restricción para registro de productos con valores 0 en la rebición de productos para el CheckIN en visitas a PDV.
        - Se filtra listado de PDVs por ID FDV para Control PDV->Punto de oportunidad.
    - **14/01/220:**
        - Se filtra el listado de los PDVs por ID FDV para Control PDV->Mis pedidos.
        - Se agrega al filtro de los PDV para el agendamiento el ID del PDV.
        - Se corrige el registro del alerta para enviar notificación al JDV.
- **1.7** Correcciones
    - El registro de inicio y fin de visitas se registra desde la aplicación en todo momento, ya no lo hace el socket de posición en tiempo real.
- **1.8**
    - Agregado filtro de categorías para CONTROL PDV -> Pundo de oportunidad