package myWhats;

import java.util.Scanner;
import static myWhats.MyWhatsStub.*;

/**
 * Classe que implementa o cliente da aplicação, recebendo 
 * comandos do utilizador
 * @author Telmo Santos 44839, Luís Carvalho 44907
 *
 */
public class MyWhats {

	/**
	 * Executa o cliente, recebendo os comandos do utilizador
	 * @param args parametros de entrada:
	 * 			   Index 0 -> nome do utilizador
	 * 			   Index 1 -> endereço e porto do servidor
	 *             Paramtros opcionais:
	 *             -> -p palavra-passe
	 *             -> -m contacto messagem
	 *             -> -f contacto ficheiro
	 *             -> -r contacto ficheiro
	 *             -> -a utilizador grupo
	 *             -> -d utilizador grupo
	 * 				
	 */
	public static void main(String[] args) {
        
		if(args.length < 2){
			System.out.println("Uso errado. Como correr: ");
			System.out.println("myWhats <nome do utilizador> <endereço:porto do servidor>");
			System.out.println("Comandos opcionais:");
			System.out.println("-p <palavra-passe>");
			System.out.println("-m <contacto> <messagem>");
			System.out.println("-f <contacto>  <ficheiro>");
			System.out.println("-r");
			System.out.println("-r <contacto>");
			System.out.println("-r <contacto> <ficheiro>");
			System.out.println("-a <utilizador> <grupo>");
			System.out.println("-d <utilizador> <grupo>");
			System.exit(0); // para nao ir para ao código de baixo
        }
        
        String password = null;
        
        int index = -1;
        
        if(args.length > 2 && args[2].equals("-p")){
            if(args.length >= 4) {
                password = args[3];
                index = 4;   
            }   
            else if(args.length == 3) { 
                password = pedirPassword();
                
                String result = createUser(args[0], password, args[1]);
                 
                System.out.println(result);                 
                System.exit(0);
           }
        }else{
            password = pedirPassword();
            index = 2;
        }
        
        if(args.length >= index + 1)  {
            switch(args[index]){
                case "-m":
                    System.out.println(args.length > index + 2 ? sendMessage(args[0], password, args[1], args[index + 1], args[index + 2]) : "Faltam argumentos: -m <contacto> <messagem>");
                    break;
                
                case "-f":
                	System.out.println(args.length > index + 2 ? sendFile(args[0], password,args[1], args[index + 1], args[index + 2]) : "Faltam argumentos: -f <contacto> <ficheiro>");
                    break;
                 
                case "-r":
                    if(args.length == index + 1)
                    	System.out.println(getMostRecentCommunications(args[0], password, args[1]));
                    else if(args.length == index + 2)
                    	System.out.println(getAllContactCommunications(args[0], password, args[1], args[index + 1]));
                    else if(args.length == index + 3)
                    	System.out.println(getContactFile(args[0], password, args[1], args[index + 1], args[index + 2]));
                    break;
                
                case "-a":
                	System.out.println(args.length > index + 2 ? addToGroup(args[0], password, args[1], args[index + 1], args[index + 2]) : "Faltam argumentos: -a <contacto> <grupo>");
                    break;
                
                case "-d":
                	System.out.println(args.length > index + 2 ? removeFromGroup(args[0], password,args[1], args[index + 1], args[index + 2]) : "Faltam argumentos: -d <contacto> <grupo>");
                    break;
                    
                default: System.out.println("Comando não reconhecido.");
            }
        }
        else {
            String result = createUser(args[0], password, args[1]);
            System.out.println(result);                 
        }
    }    
    
	/**
	 * Pede ao utilizador que insira a paalavra-passe
	 * @return palavra-passe inserida
	 */
    private static String pedirPassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira password: ");
        String password = sc.nextLine();
        sc.close();
        return password;
    }
}

