
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{
    //O gerenciador eh "o cliente", ele vai ter os atributos como cache, status de online etc
    static Vector <Gerenciador> vect = new Vector<>();
    static int NumDeClientes = 0;
    static DefaultListModel<String> cache = new DefaultListModel<>();

    public static void main(String[] args){
        //o main so executa o servidor easy peasy
        Server servidor = new Server();
    }
    private Server() //classe do servidor, que executa as threads
    {
        //a porta usada para comunicacao
        int porta = 12345;
        //inicializacao do socket por causa do try catch :/
        ServerSocket socketServer = null;
        try {
            socketServer = new ServerSocket(porta);
        } catch (Exception e) {
            //ele tem q estar no bloco try catch :/
        }
        Socket novoSocket;


        //os "clientes" vao ser criados a partir de agora
        while (true) //loop infinito :(
        {
            //inicializacao dos inputs e outputs e do novo socket, isso para o novo cliente

            DataInputStream inputData = null;
            DataOutputStream outputData = null;

            novoSocket = null;
            try {
                novoSocket = socketServer.accept(); //tcp :p
                inputData = new DataInputStream(novoSocket.getInputStream());
                outputData = new DataOutputStream(novoSocket.getOutputStream());
            } catch (Exception e) {
                //eles tem q estar no bloco try catch :/
            }


            //inicializando um novo gerente para o novo cliente.
            Gerenciador novo = new Gerenciador(NumDeClientes,novoSocket, inputData, outputData);
            //inicia a thread, p poder receber msgs de todos ao msm tempo :p
            Thread THREAD = new Thread(novo);
            //cliente adicionado a lista de clientes ativos
            vect.add(novo);
            THREAD.start();
            NumDeClientes++;
        }
    }
}
class Gerenciador implements Runnable {
    Scanner in = new Scanner(System.in);
    //lista de atributos do "cliente" como eu disse la em cima, so coloquei as necessarias para o funcionamento mais basico
    final DataInputStream inputData;
    final DataOutputStream outputData;
    Socket novoSocket;
    String nome;
    boolean checkNome;
    int id = 0;

    // construtor
    public Gerenciador(int id, Socket novoSocket, DataInputStream inputData, DataOutputStream outputData) {
        this.inputData = inputData;
        this.outputData = outputData;
        this.novoSocket = novoSocket;
        this.id = id;
        nome = "";
        checkNome = false;
    }

    public void run() {
        //aqui eh o q vai rolar dentro da thread, a unica coisa importante daqui eh a mensagem, pois eh a unica funcao do
        //servidor, receber mensagens :p
        //Os blocos try catch sao por causa dos erros :)
        String mensagem;
        while (true)
        {
            try
            {
                if(!checkNome){
                    String inserirNome = inputData.readUTF();
                    boolean unicoNome = true;
                    for(Gerenciador cliente: Server.vect){
                        if(cliente.nome.equals(inserirNome)) {
                            unicoNome = false;
                        }
                    }
                    if(unicoNome) {
                        checkNome = true;
                        this.nome = inserirNome;
                        this.outputData.writeUTF("Bem vindo ao chat, " + this.nome);
                    } else {
                        this.outputData.writeUTF("Nome já existente, tente novamente.");
                    }

                } else {
                    //PARTE IMPORTANTE
                    mensagem = inputData.readUTF();
                    Server.cache.addElement(mensagem);

                    if(mensagem.contains("COMMAND=DELETE")){
                        mensagem.replace("COMMAND=DELETE", "");
                        for (Gerenciador cliente : Server.vect) {
                            if(cliente.id == this.id)
                                cliente.outputData.writeUTF("Você: " + mensagem);
                            else
                                cliente.outputData.writeUTF("COMMAND=DELETE=" + this.nome + ": "+ mensagem);
                                //IMPLEMENTAR CONDIÇÃO PARA APAGAR MENSAGEM
                        }
                    } else {
                        //por exemplo, para reenviar as mensagens recebidas p todo mundo, eh so fazer
                        for (Gerenciador cliente : Server.vect) {
                            if(cliente.id == this.id)
                                cliente.outputData.writeUTF("Você: " + mensagem);
                            else
                                cliente.outputData.writeUTF(this.nome + ": "+ mensagem);
                        }
                    }


                    if (false) {//essa aberracao so existe pq se nao da erro de codigo inalcancavel
                        break;
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
//        try
//        {
//            // closing resources
//            this.inputData.close();
//            this.outputData.close();
//
//        }catch(IOException e){
//            e.printStackTrace();
//        }
    }
}
