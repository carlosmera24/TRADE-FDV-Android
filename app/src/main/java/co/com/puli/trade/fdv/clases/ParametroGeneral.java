package co.com.puli.trade.fdv.clases;

/**
 * Clase utilizda para registrar objeto para la tabla tblParametrosGenerales
 * Created by carlos on 29/11/16.
 */

public class ParametroGeneral
{
    String id, key, var1, var2, var3, var4, var5;

    /**
     * Constructur con los campos de la tabla
     * @param id IDParametrosGenerales
     * @param key keyParametrosGenerales
     * @param var1 var1ParametrosGenerales Varchar
     * @param var2 var2ParametrosGenerales Text
     * @param var3 var3ParametrosGenerales Int
     * @param var4 var4ParametrosGenerales Decimal(15,7)
     * @param var5 var5ParametrosGenerales DataTime
     * */
    public ParametroGeneral(String id, String key, String var1, String var2, String var3, String var4, String var5) {
        this.id = id;
        this.key = key;
        this.var1 = var1;
        this.var2 = var2;
        this.var3 = var3;
        this.var4 = var4;
        this.var5 = var5;
    }

    /**
     * @return id IDParametrosGenerales
     * */
    public String getId() {
        return id;
    }

    /**
     * @param id IDParametrosGenerales
     * */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return key keyParametrosGenerales
     * */
    public String getKey() {
        return key;
    }

    /**
     * @param key keyParametrosGenerales
     * */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return var1 var1ParametrosGenerales Varchar
     * */
    public String getVar1() {
        return var1;
    }

    /**
     * @param var1 var1ParametrosGenerales Varchar
     * */
    public void setVar1(String var1) {
        this.var1 = var1;
    }

    /**
     * @return var2 var2ParametrosGenerales Text
     * */
    public String getVar2() {
        return var2;
    }

    /**
     * @param var2 var2ParametrosGenerales Text
     * */
    public void setVar2(String var2) {
        this.var2 = var2;
    }

    /**
     * @return var3 var3ParametrosGenerales Int
     * */
    public String getVar3() {
        return var3;
    }

    /**
     * @param var3 var3ParametrosGenerales Int
     * */
    public void setVar3(String var3) {
        this.var3 = var3;
    }

    /**
     * @return var4 var4ParametrosGenerales Decimal(15,7)
     * */
    public String getVar4() {
        return var4;
    }

    /**
     * @param var4 var4ParametrosGenerales Decimal(15,7)
     * */
    public void setVar4(String var4) {
        this.var4 = var4;
    }

    /**
     * @return var5 var5ParametrosGenerales DataTime
     * */
    public String getVar5() {
        return var5;
    }

    /**
     * @param var5 var5ParametrosGenerales DataTime
     * */
    public void setVar5(String var5) {
        this.var5 = var5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParametroGeneral)) return false;

        ParametroGeneral that = (ParametroGeneral) o;

        if (!getId().equals(that.getId())) return false;
        return getKey().equals(that.getKey());

    }

    @Override
    public int hashCode() {
        int result = getId().hashCode();
        result = 31 * result + getKey().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ParametroGeneral{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", var1='" + var1 + '\'' +
                ", var2='" + var2 + '\'' +
                ", var3='" + var3 + '\'' +
                ", var4='" + var4 + '\'' +
                ", var5='" + var5 + '\'' +
                '}';
    }
}
