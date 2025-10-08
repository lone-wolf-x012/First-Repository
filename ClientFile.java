import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientFile {
    static final String HOST = "localhost";
    static final int PUERTO = 5001;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PUERTO);
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());
            Scanner sc = new Scanner(System.in)
        ) {
            System.out.println("Conectado al servidor.");

            while (true) {
                System.out.println("\nMenú:");
                System.out.println("1. Sendfile");
                System.out.println("2. Getfile");
                System.out.println("3. Exit");
                System.out.print("Seleccione opción: ");
                int opcion = sc.nextInt();
                sc.nextLine(); // limpiar buffer

                if (opcion == 1) {
                    salida.writeUTF("Sendfile");
                    System.out.print("Ingrese ruta completa del archivo a enviar: ");
                    String ruta = sc.nextLine();
                    File file = new File(ruta);

                    if (!file.exists()) {
                        System.out.println("El archivo no existe.");
                        continue;
                    }

                    salida.writeUTF(file.getName());
                    salida.writeLong(file.length());

                    try (FileInputStream fis = new FileInputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int leidos;
                        while ((leidos = fis.read(buffer)) != -1) {
                            salida.write(buffer, 0, leidos);
                        }
                    }

                    System.out.println("Servidor: " + entrada.readUTF());

                } else if (opcion == 2) {
                    salida.writeUTF("Getfile");
                    System.out.print("Ingrese el nombre del archivo que desea descargar: ");
                    String nombreArchivo = sc.nextLine();
                    salida.writeUTF(nombreArchivo);

                    String respuesta = entrada.readUTF();
                    if (!respuesta.equals("OK")) {
                        System.out.println("Servidor: " + respuesta);
                        continue;
                    }

                    long tamano = entrada.readLong();
                    File file = new File(nombreArchivo);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        long bytesRecibidos = 0;
                        int leidos;

                        while (bytesRecibidos < tamano && (leidos = entrada.read(buffer)) != -1) {
                            fos.write(buffer, 0, leidos);
                            bytesRecibidos += leidos;
                        }
                    }

                    System.out.println("Archivo descargado exitosamente: " + nombreArchivo);

                } else if (opcion == 3) {
                    salida.writeUTF("Exit");
                    System.out.println("Desconectando...");
                    break;
                } else {
                    System.out.println("Opción inválida.");
                }
            }
        } catch (IOException e) {
            System.out.println("Error en cliente: " + e.getMessage());
        }
    }
}
