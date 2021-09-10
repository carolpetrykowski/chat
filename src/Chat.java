import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ObjectMessage;
import org.jgroups.View;
import org.jgroups.Receiver;


public class Chat implements Receiver{
	
	JChannel channel;
	String username = System.getProperty("user.name", "n/a");
	final List<String> state = new LinkedList<String>();
	
	public void viewAccepted(View newView) {
		System.out.println("** view: " + newView);
	}
	
	public void receive(Message msg) {
		System.out.println(msg.getSrc() + ": " + msg.getObject());
	}	
	
	private void start() throws Exception{
		/*
		 *  JChannel()
		 *  Instanciando JChannel
		 *  O construtor vazio instancia o channel com configs padrões
		 *  Seria possível usar um XML para configurá-lo
		 *  
		 *  setReceiver(this)
		 *  Permite o recebimento de mensagens
		 *  
		 *  connect("Chat")
		 *	Efetivamente conecta no cluster
		 *	Todas instâncias que chamarem connect com o mesmo arg conectam no mesmo cluster
		 *	Logo, aqui todos iram conectar no cluster chamado 'Chat'
		 */
		channel = new JChannel().setReceiver(this).connect("Chat");
		
		/*
		 * 1º arg: instância alvo, null significa o coordenador
		 * 2º arg: timeout (10s)
		*/ 
		eventLoop(); // Loop para troca de mensagens
		channel.close(); // Fecha o channel
	}
	
	/*
	 * Loop infinito que lê strings e envia para todas as instâncias do cluster
	 * Quando 'sair' ou 'quit' for digitado o loop é encerrado e o channel finalizado 
	*/
	private void eventLoop() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				System.out.print("> "); System.out.flush();
				String line = in.readLine().toLowerCase();
				if(line.startsWith("sair") || line.startsWith("exit")){
					break;
				}
				line = "[" + username + "] " + line;
				/*
				 * 1º arg: endereço de destino, null envia para todos no cluster
				 * 2º arg: mensagem que será enviada, usa serialização Java para criar um buffer de byte[] e enviar o payload de mensagem
				*/
				Message msg = new ObjectMessage(null, line);
				channel.send(msg);
			}
			catch(Exception e) {
			}
		}
	}
	
	public static void main(String[] args) throws Exception{
		new Chat().start();
	}
}
