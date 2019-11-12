/*

O resultado final do programa podería amosarse de modo  similar ao seguinte :

CODIGO DO PLATO : p1
nome do plato : platocarnico1
codigo do componente : c1-> graxa por cada 100 gr= 5
peso : 400
total de graxa do componente = 20

codigo do componente : c3-> graxa por cada 100 gr= 10
peso : 600
total de graxa do componente = 60

TOTAL EN GRAXAS DO PLATO: 80

CODIGO DO PLATO : p2
nome do plato : platocarnico2
codigo do componente : c2-> graxa por cada 100 gr= 20
peso : 600
total de graxa do componente = 120

codigo do componente : c3-> graxa por cada 100 gr= 10
peso : 300
total de graxa do componente = 30

codigo do componente : c4-> graxa por cada 100 gr= 5
peso : 200
total de graxa do componente = 10

TOTAL EN GRAXAS DO PLATO: 160


notas para os calculos :  
para calcular a graxa dun compoñente dun plato habera que multiplicar a graxa do compoñente por o seu peso no prato e dividilo por 100 .

por exemplo . 
O contido en graxa do compoñente 'c1' correspondente ao prato de codigo 'p1' sera:
5(graxa)*400(peso)/100= 20
O contido en graxa do compoñente 'c3' correspondente ao prato de codigo 'p1' sera :
10(graxa)*600(peso)/100= 60
Por isto o contido en graxas totais do prato p1 e de 20+60  = 80 unidades.

*/

package exa15;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;


public class Exa15 {
    
    public static Connection conexion=null;
    static File txt = new File("/home/oracle/Desktop/compartido/Exa15/ExExameAD/platoss");
    
    public static Connection getConexion() throws SQLException  {
        String usuario = "hr";
        String password = "hr";
        String host = "localhost"; 
        String puerto = "1521";
        String sid = "orcl";
        String ulrjdbc = "jdbc:oracle:thin:" + usuario + "/" + password + "@" + host + ":" + puerto + ":" + sid;
        
           
            conexion = DriverManager.getConnection(ulrjdbc);
            return conexion;
        }
     
     public static void closeConexion() throws SQLException {
      conexion.close();
      }
     
     public static ArrayList<Platos> leerPlatos(File txt){
        ArrayList<Platos> platosLeidos = new ArrayList<>(); 
        ObjectInputStream read;  
        try {
            read = new ObjectInputStream(new BufferedInputStream(new FileInputStream(txt)));
            while (true) {
                Object obj = read.readObject();
                if (obj == null) {
                    break;
                }
                platosLeidos.add((Platos) obj);
            }
            read.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return platosLeidos;
     } 
     
     public static HashMap<String, Integer> SelectComposicion(String codp) throws SQLException{
         String sql = "Select * from composicion where codp='"+codp+"'";
        HashMap<String, Integer> composicion = new HashMap<>();
         //conexion
            Connection conn = getConexion();
            //intermediario
            Statement statement = conn.createStatement();
            //resultados
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                composicion.put(rs.getNString("codc"), Integer.parseInt(rs.getNString("peso")));  
            }
        return composicion;
     }

     public static Integer SelectGrasa(String codc) throws SQLException{
         String sql = "Select graxa from componentes where codc='"+codc+"'";
        int grasa = 0;
         //conexion
            Connection conn = getConexion();
            //intermediario
            Statement statement = conn.createStatement();
            //resultados
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                grasa = Integer.parseInt(rs.getNString("graxa"));  
            }
        return grasa;
     }

     public static Integer CalcularContenido(int peso, int grasa){
         int contGrasa = peso/100 * grasa;
        return contGrasa;
     }
     
     public static void EscribirXML() throws IOException, SQLException{
         
        File xml = new File("/home/oracle/Desktop/compartido/Exa15/ExExameAD/platos.xml");
        
        try {
            XMLStreamWriter xmlSW = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(xml));

            //VersionXML DeclaracionInicial
            xmlSW.writeStartDocument("1.0");

            //EtiquetaRaizApertura
            //<platos>
            xmlSW.writeStartElement("platos");

             for (Platos plato : leerPlatos(txt)){

                //<plato codigop="cod">
                xmlSW.writeStartElement("plato");
                xmlSW.writeAttribute("codigop", plato.getCodigop());

                //<nomep>
                xmlSW.writeStartElement("nomep");
                xmlSW.writeCharacters(plato.getNomep());
                //</nomep>
                xmlSW.writeEndElement();
                
                int totalGrasa = 0;
                for (Entry<String,Integer> entry : SelectComposicion(plato.getCodigop()).entrySet()) {
                  totalGrasa += CalcularContenido(entry.getValue(), SelectGrasa(entry.getKey()));       
                 }
                //<graxaTotal>
                xmlSW.writeStartElement("graxaTotal");
                xmlSW.writeCharacters(Integer.toString(totalGrasa));
                //</graxaTotal>
                xmlSW.writeEndElement();
                
                //</plato>
                xmlSW.writeEndElement();
            }

            //EtiquetaRaizCierre
            //</platos>
            xmlSW.writeEndElement();
            xmlSW.close();
        } catch (XMLStreamException ex) {
            ex.printStackTrace();
        } 
     }
     
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException, XMLStreamException {
    
    for (Platos plato : leerPlatos(txt)){
        System.out.println("CODIGO DO PLATO : " +plato.getCodigop()+ "\n" +
                           "nome do plato : " +plato.getNomep());
        int totalGrasa = 0;
     for (Entry<String,Integer> entry : SelectComposicion(plato.getCodigop()).entrySet()) {
         System.out.println("codigo do componente : "+entry.getKey()+"-> graxa por cada 100 gr= "+SelectGrasa(entry.getKey())+"\n" +
                            "peso : "+entry.getValue()+"\n" +
                            "total de graxa do componente = " + CalcularContenido(entry.getValue(), SelectGrasa(entry.getKey())) + "\n");
        totalGrasa += CalcularContenido(entry.getValue(), SelectGrasa(entry.getKey()));       
     }
        System.out.println("TOTAL EN GRAXAS DO PLATO:" + totalGrasa + "\n");
    }
    
    EscribirXML();
    closeConexion();       
    }
}
      

