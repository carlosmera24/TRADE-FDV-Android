package co.com.puli.trade.fdv.actividades;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mousebird.maply.BaseController;
import com.mousebird.maply.ComponentObject;
import com.mousebird.maply.GlobeMapFragment;
import com.mousebird.maply.MapController;
import com.mousebird.maply.MarkerInfo;
import com.mousebird.maply.Point2d;
import com.mousebird.maply.QuadImageLoader;
import com.mousebird.maply.RemoteTileInfoNew;
import com.mousebird.maply.RenderController;
import com.mousebird.maply.SamplingParams;
import com.mousebird.maply.ScreenMarker;
import com.mousebird.maply.SelectedObject;
import com.mousebird.maply.SphericalMercatorCoordSystem;
import com.mousebird.maply.VectorInfo;
import com.mousebird.maply.VectorObject;
import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.Utilidades;
import java.io.File;

/**
 * Fragment para control y gestión de la visualización del Mapa OSM
 */
public class MapaFragment extends GlobeMapFragment {
    private double MAX_ZOOM = 18;
    private double BASE_ELEVATION = 0.06;
    private ComponentObject coMarkers = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container, savedInstanceState);
        return baseControl.getContentView();
    }

    @Override
    protected MapDisplayType chooseDisplayType() {
        return MapDisplayType.Map;
    }

    @Override
    protected void controlHasStarted() {
        super.controlHasStarted();

        // setup up the local cache directory
        String cacheDirName = "stamen_puli_map_osm";
        File cacheDir = new File(getActivity().getCacheDir(), cacheDirName);
        cacheDir.mkdir();

        //Set up access to the tile images
        RemoteTileInfoNew remoteTileSource = new RemoteTileInfoNew(
                getString( R.string.url_server_tiles_map_osm ) + "{z}/{x}/{y}.png",
                0,
                18);
        remoteTileSource.cacheDir = cacheDir;

        //Set up the globe parameters
        SamplingParams params = new SamplingParams();
        params.setCoordSystem( new SphericalMercatorCoordSystem() );
        params.setSingleLevel(false);
        params.setCoverPoles(true);
        params.setEdgeMatching(true);
        params.setMinZoom( remoteTileSource.minZoom );
        params.setMaxZoom( remoteTileSource.maxZoom );

        //Set up an image loader
        QuadImageLoader loader = new QuadImageLoader( params, remoteTileSource, baseControl);
        loader.setImageFormat(RenderController.ImageFormat.MaplyImageIntRGBA);

        mapControl.animatePositionGeo( degressToRadians(-74.058838), degressToRadians( 4.668821), 0.2, 1.0 );
        mapControl.setAllowRotateGesture(true);
        mapControl.gestureDelegate = this; //Gestionar el toque sobre el mapa/marcadores
    }

    @Override
    public void userDidSelect(MapController mapControl, SelectedObject[] selObjs, Point2d loc, Point2d screenLoc) {
        super.userDidSelect(mapControl, selObjs, loc, screenLoc);

        //Recorrer el objecto seleccionado, puede ser un vector o un marcador
        for( SelectedObject obj : selObjs )
        {
            //Si el objecto seleccionado es un marcador
            if( obj.selObj instanceof ScreenMarker )
            {
                ScreenMarker screenMarker = (ScreenMarker) obj.selObj;
                //Obtener el label del mismo
                if( screenMarker.userObject != null ) {
                    String label_marker = (String) screenMarker.userObject;
                    new Utilidades().mostrarSimpleMensaje( getContext(), null, label_marker, false );
                }
            }
        }
    }

    /**
     * Método ecargado de mover la camara en el mapa
     * @param lat Latitud de la coordenada
     * @param lng Longitud de la coordenada
     * @param zoom de la vista del mapa
     * */
    public void moverCamara( double lat, double lng, int zoom)
    {
        if( mapControl != null )
        {
            mapControl.animatePositionGeo( degressToRadians(lng), degressToRadians(lat), getHeightZElevation( zoom ), 1.0 );
        }
    }

    /**
     * Método encargado de agegar marcador al mapa
     * @param lat Latitud de la ubicación del marcador
     * @param lng Longitud de la ubicación del marcador
     * @param icon Bitmap del icono para el marcador, puede usar BitmapFactory.decodeResource(getResources(), R....)
     * @param label Texto para visualizar al tocar el marcador
     * */
    public void agregarMarcador(double lat, double lng, Bitmap icon, String label )
    {
        MarkerInfo markerInfo = new MarkerInfo();
        Point2d markerSize = new Point2d(144, 144);

        ScreenMarker marker= new ScreenMarker();
        marker.loc = Point2d.FromDegrees(lng, lat);
        marker.image = icon;
        marker.size = markerSize;
        marker.userObject = label; //Se agregar el label como objeto del usuario
        marker.selectable = true; //Habilitar selección del marcador

        mapControl.addScreenMarker( marker, markerInfo, BaseController.ThreadMode.ThreadAny );
    }

    /**
     * Método encargado de mover marcador del mapa
     * -Evalua si existe algun marcador, en caso tal lo elimina y crea uno nuevo para emular el desplazamiento de un único marcador
     * @param lat Latitud de la ubicación del marcador
     * @param lng Longitud de la ubicación del marcador
     * @param icon Bitmap del icono para el marcador, null si se desea conservar el anterior, puede usar BitmapFactory.decodeResource(getResources(), R....)
     * @param label Texto para visualizar al tocar el marcador
     * */
    public void moverMarcador(double lat, double lng, Bitmap icon, String label)
    {
        if( coMarkers != null ) {
            //Eliminar marcadores
            mapControl.removeObject(coMarkers, BaseController.ThreadMode.ThreadCurrent);
        }

        //Crear nuevo marcador
        MarkerInfo markerInfo = new MarkerInfo();
        Point2d markerSize = new Point2d(144, 144);
        ScreenMarker marker= new ScreenMarker();
        marker.loc = Point2d.FromDegrees(lng, lat);
        marker.image = icon;
        marker.size = markerSize;
        marker.userObject = label;
        marker.selectable = true; //Habilitar selección del marcador

        //Agregar nuevo marcador y guardarlo como ComponetObjet marker
        coMarkers = mapControl.addScreenMarker( marker, markerInfo, BaseController.ThreadMode.ThreadCurrent);
    }

    /**
     * Método encargado de agregar un vector de obejtos al maapa, trazando una ruta
     * */
    public void agregarVectorObject( VectorObject vo)
    {
        VectorInfo vectorInfo = new VectorInfo();
        vectorInfo.setColor( Color.parseColor("#008df2") );
        vectorInfo.setLineWidth( 6.f );
        mapControl.addVector(vo, vectorInfo, BaseController.ThreadMode.ThreadAny);
    }

    /**
     * Método encargado de convertir Grados (Lat o Lng) a Radianes
     * @param coord Lat o Lng a convertir
     * @return double valor radian
     * */
    public double degressToRadians( double coord )
    {
        return ( coord * Math.PI ) / 180;
    }

    /**
     * Método encargado de generar la altura de la vista del mapa
     * @param zoom 1-19
     * @return double altura
     * */
    private double getHeightZElevation( int zoom )
    {
        zoom = zoom > MAX_ZOOM ? (int) MAX_ZOOM : zoom;
        return ( BASE_ELEVATION / MAX_ZOOM ) / zoom;
    }
}