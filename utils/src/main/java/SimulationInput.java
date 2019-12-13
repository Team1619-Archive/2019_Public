import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class SimulationInput {

	public static void main(String[] args) throws IOException, InterruptedException {
		Socket client = new Socket();
		client.connect(new InetSocketAddress(11619));

		PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("Enter event <type> <input or fms_mode> [options...]");
			String response = scanner.nextLine();

			if ("exit".equals(response)) {
				break;
			}

			writer.println(response);
		}
	}
}
