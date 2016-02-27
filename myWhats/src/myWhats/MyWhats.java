package myWhats;

import java.util.Scanner;

public class MyWhats {

	public static void main(String[] args) {
        
		if(args.length < 3){
			System.out.println("Uso errado. Comandos possiveis :");
			System.exit(0); // para nao ir para ao código de baixo
        }
        
        String password = null;
        
        int index = -1;
        
        if(args[2].equals("-p")){
            if(args.length >= 4) {
                password = args[3];
                index = 4;   
            }   
            else if(args.length == 3) { 
                password = pedirPassword();
                
                boolean registou = MyWhatsStub.criaConta(args[0], password);
                
                if(registou)
                    System.out.println("Conta não existia e foi criada com sucesso.");
                else
                    System.out.println("A conta já se encontra registada.");   
                                 
                System.exit(0);
           }
        }else{
            password = pedirPassword();
            index = 2;
        }
        
        if(args.length >= index + 1)  {
            switch(args[index]){
                case "-m":
                    boolean result = args.length > index + 2 ? sendMessage(args[index+1],args[index+2],args[0],password) : faltamArgumentos();
                    break;
                
                case "-f":
                    args.length > index + 2 ? enviarFicheiro(args[index+1],args[index+2],args[0],password) : faltamArgumentos();
                    break;
                 
                case "-r":
                    if(args.length == index)
                        comunicacoesMaisRecentes(args[0],password);
                    else if(args.length == index + 1)
                        todasComunicacoes(args[index + 1], args[0],password);
                    else if(args.length == index + 2)
                        buscFicheiro(args[index + 1], args[index + 2], args[0],password);
                    break;
                
                case "-a":
                    args.length > index + 2 ? adicionarAoGrupo(args[index+1],args[index+2],args[0],password) : faltamArgumentos();
                    break;
                
                case "-d":
                    args.length > index + 2 ? removerDoGrupo(args[index+1],args[index+2],args[0],password) : faltamArgumentos();
                    break;
                
            }
            
        }
    }    
    
    private static String pedirPassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Insira password : ");
        String password = sc.nextLine();
        sc.close();
        return password;
    }    
}

