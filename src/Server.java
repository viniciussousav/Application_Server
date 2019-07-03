import javax.swing.*;
import java.io.*;
import java.util.*;
import java.net.*;

public class Server
{
    static Vector <Gerenciador> vect = new Vector<>();
    static int NumDeClientes = 0;
    static DefaultListModel<String> cache = new DefaultListModel<>();

    public static void main(String[] args){
        Server servidor = new Server();
    }
    private Server() {
        int porta = 12345;
        ServerSocket socketServer = null;
        try {
            socketServer = new ServerSocket(porta);
        } catch (Exception e) { }
        Socket novoSocket;

        while (true) {

            DataInputStream inputData = null;
            DataOutputStream outputData = null;

            novoSocket = null;
            try {
                novoSocket = socketServer.accept(); //tcp :p
                inputData = new DataInputStream(novoSocket.getInputStream());
                outputData = new DataOutputStream(novoSocket.getOutputStream());
            } catch (Exception e) {
            }

            Gerenciador novo = new Gerenciador(NumDeClientes,novoSocket, inputData, outputData);
            Thread THREAD = new Thread(novo);
            vect.add(novo);
            THREAD.start();
            NumDeClientes++;
        }
    }
}

class Gerenciador implements Runnable {
    Scanner in = new Scanner(System.in);
    final DataInputStream inputData;
    final DataOutputStream outputData;
    Socket novoSocket;
    String nome;
    boolean checkNome;
    int id = 0;
    boolean online;

    // construtor
    public Gerenciador(int id, Socket novoSocket, DataInputStream inputData, DataOutputStream outputData) {
        this.inputData = inputData;
        this.outputData = outputData;
        this.novoSocket = novoSocket;
        this.id = id;
        nome = "";
        checkNome = false;
        online = true;
    }

    public void run() {
        String mensagem;
        while (online)
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
                        this.outputData.writeUTF("Bem vindo ao WhatsMyApp(Infracom 2019.1), " + this.nome);
                        this.outputData.writeUTF("Feito por: Marcela, Pedro, Vinicius, Gabriela, Hugo e Luan");
                        this.outputData.writeUTF("Para sair digite: FINALIZAR() e então feche a janela");
                        String usuariosOnline = "";
                        for (Gerenciador cliente: Server.vect){
                            if (!cliente.nome.isEmpty() && cliente.id != this.id && cliente.online) {
                                usuariosOnline += "| " + cliente.nome;
                            }
                        }
                        this.outputData.flush();
                        if(usuariosOnline.isEmpty()) {
                            this.outputData.writeUTF("Ainda não há usuários online no chat");
                        } else {
                            this.outputData.writeUTF("Usúarios no chat: " + usuariosOnline);
                        }
                        for (Gerenciador cliente : Server.vect) {
                            if(cliente.id != this.id && cliente.online)
                                cliente.outputData.writeUTF(this.nome + " entrou no chat");
                        }

                    } else {
                        this.outputData.writeUTF("Nome já existente, tente novamente.");
                    }

                } else {
                    mensagem = inputData.readUTF();
                    Server.cache.addElement(mensagem);

                    if(mensagem.contains("COMMAND=DELETE:")){
                        mensagem = mensagem.replace("COMMAND=DELETE:", "");
                        for (Gerenciador cliente : Server.vect) {
                            if(cliente.id != this.id && cliente.online)
                                cliente.outputData.writeUTF("COMMAND=DELETE:" + this.nome + ": "+ mensagem);
                        }
                    } else if(mensagem.equals("FINALIZAR()")){
                        for (Gerenciador cliente: Server.vect){
                            if(cliente.id != this.id && cliente.online)
                                cliente.outputData.writeUTF(this.nome + " saiu do chat.");
                        }
                        this.outputData.writeUTF("FINALIZAR()");
                        this.outputData.close();
                        this.inputData.close();
                        this.online = false;
                        this.nome = "";

                    }else {
                        Vector<String> recebidoVec = new Vector<>();
                        for (Gerenciador cliente : Server.vect) {
                            if(cliente.id != this.id && cliente.online){
                                cliente.outputData.writeUTF(this.nome + ": "+ mensagem);
                                recebidoVec.add(cliente.nome);
                            }
                        }

                        if(recebidoVec.size() == 0){
                            this.outputData.writeUTF("Você: " + mensagem + " (Ninguém recebeu a mensagem)");
                        } else {
                            String recebidoMensagem = "";
                            for (int i = 0; i < recebidoVec.size(); i++){
                                if(i == recebidoVec.size() - 1)
                                    recebidoMensagem += recebidoVec.elementAt(i) + ".";
                                else
                                    recebidoMensagem += recebidoVec.elementAt(i) + ", ";
                            }
                            this.outputData.writeUTF("Você: " + mensagem + " (Mensagem recebida por: " + recebidoMensagem + ")") ;
                        }
                    }
                    if (false) {
                        break;
                    }
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        try
        {
            this.inputData.close();
            this.outputData.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
}