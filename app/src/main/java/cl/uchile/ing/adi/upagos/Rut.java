package cl.uchile.ing.adi.upagos;

/**
 * Clase para obtener dígito verificador.
 * Basado en la implementación de Luis Dujovne en
 * http://users.dcc.uchile.cl/~mortega/microcodigos/validarrut/java.html
 */
public class Rut {
    public static char getDV(String rut){
        try {
            int m = 0;
            int s = 1;
            int intRut = Integer.parseInt(rut);
            for (; intRut != 0; intRut /= 10) s = (s + intRut % 10 * (9 - m++ % 6)) % 11;
            char dv = (char) (s != 0 ? s + 47 : 75);
            return dv;
        }
        catch (Exception e){ return 0;}
    }
}
