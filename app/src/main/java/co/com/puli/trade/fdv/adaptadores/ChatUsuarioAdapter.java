package co.com.puli.trade.fdv.adaptadores;


import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import co.com.puli.trade.fdv.R;
import co.com.puli.trade.fdv.clases.UsuarioChat;

import java.util.ArrayList;

/**
 * Created by carlos on 17/02/16.
 */
public class ChatUsuarioAdapter extends FragmentPagerAdapter
{
    private final int PAGE_COUNT = 2; //Numero de pestañas
    private Context context;
    private String titulos[], id_usuario;
    private ArrayList<UsuarioChat> usuarios;

    /**
     * Constructor del adaptador para ViewPager
     * @param fm instancia del FragmentManager de la actividad en ejecución, usar getSupportFragmentManager
     * @param context Context de la instancia de la actividad en ejecución, usar (this o ActividadName.this)
     * */
    public ChatUsuarioAdapter(FragmentManager fm, Context context, ArrayList<UsuarioChat> usuarios, String id_usuario)
    {
        super(fm);
        this.context = context;
        titulos = new String[]
                {
                        context.getString( R.string.txt_puntos_de_venta ),
                        context.getString( R.string.txt_coordinaores )
                };
        this.usuarios = usuarios;
        this.id_usuario = id_usuario;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return ChatUsuariosFragment.newInstance(position + 1, usuarios, id_usuario);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titulos[position];
    }
}
