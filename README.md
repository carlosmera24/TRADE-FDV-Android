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