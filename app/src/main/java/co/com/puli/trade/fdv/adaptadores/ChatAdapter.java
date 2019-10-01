package co.com.puli.trade.fdv.adaptadores;

import android.app.Activity;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.Chat;
import co.com.puli.trade.fdv.clases.CustomFonts;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Adaptador para los items del Chat
 * Created by carlos on 7/12/15.
 * Actualizado a RecyclerView 06/06/17
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>
{
    private ArrayList<Chat> mensajes;
    private CustomFonts fuentes;
    private String id_usuario;

    public ChatAdapter(ArrayList<Chat> mensajes, Activity actividad, String id_usuario)
    {
        this.mensajes = mensajes;
        this.id_usuario = id_usuario;
        fuentes = new CustomFonts( actividad.getAssets() );
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        //Definir layout para el chat
        View vista;
        Chat chat = mensajes.get( viewType ); //viewType será la posición, ya que se modificó getItemViewType para que así retorne
        if(  chat.getId_origen().equals( id_usuario) )
        {
            vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg_out, parent, false);
        }else{
            vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg_in, parent, false);
        }

        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        Chat chat = mensajes.get( position );
        holder.wvMsg.loadDataWithBaseURL(null, chat.getMensaje(), "text/html; charset=utf-8", "utf-8", null);
//        if( eliminarCache() )
//        {
//            holder.wvMsg.clearCache( true );
//        }
        holder.wvMsg.setBackgroundColor(Color.TRANSPARENT);

        holder.tvFecha.setTypeface(fuentes.getRobotoThinFont());

        //Fecha
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Calendar fecha = Calendar.getInstance();
            fecha.setTime( sdf.parse(chat.getFecha()) );
            sdf = new SimpleDateFormat("MMM dd yyyy/HH:mm");
            holder.tvFecha.setText( sdf.format(fecha.getTime()) );
        }catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Método ecargado de de retonar el tipo de view, se retorna la posición del item para evitar mal funcionamiento con los objectos,
     * es decir, para que no se repitan datos y sea coherente
     * */
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mensajes != null ? mensajes.size() : 0;
    }

    public ArrayList<Chat> getMensajes() {
        return mensajes != null ? mensajes : new ArrayList<Chat>();
    }

    public void setMensajes(ArrayList<Chat> mensajes )
    {
        this.mensajes = mensajes;
    }

    class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvFecha;
        WebView wvMsg;

        public ViewHolder(View itemView) {
            super(itemView);
            wvMsg = (WebView) itemView.findViewById( R.id.wvMsgChat );
            tvFecha = (TextView) itemView.findViewById( R.id.tvMsgFecha );
        }
    }
}
