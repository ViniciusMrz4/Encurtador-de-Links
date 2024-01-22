package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Scanner;

public class EncurtadorDeLinks {

    private static final String DATABASE_URL = "jdbc:sqlite:links.db";

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);

        System.out.println("Digite o link que deseja encurtar: ");
        String linkOriginal = sc.next();

        try {
            String linkEncurtado = encurtarLink(linkOriginal);
            System.out.println("Link encurtado: " + linkEncurtado);

            // Salvar o link encurtado no banco de dados
            inicializarBanco();
            salvarLinkNoBanco(linkOriginal, linkEncurtado);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        try {
            exibirLinksNoBanco();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encurtarLink(String linkOriginal) throws IOException {
        String apiUrl = "http://tinyurl.com/api-create.php?url=" + linkOriginal;

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.readLine();
        }
    }

    public static void inicializarBanco() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             Statement statement = connection.createStatement()) {

            // Verifica se a tabela já existe
            ResultSet resultSet = connection.getMetaData().getTables(null, null, "links", null);
            if (!resultSet.next()) {
                // Cria a tabela se não existir
                statement.executeUpdate("CREATE TABLE links (id INTEGER PRIMARY KEY AUTOINCREMENT, original TEXT NOT NULL, encurtado TEXT NOT NULL)");
            }
        }
    }

    public static void salvarLinkNoBanco(String linkOriginal, String linkEncurtado) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO links (original, encurtado) VALUES (?, ?)")) {

            statement.setString(1, linkOriginal);
            statement.setString(2, linkEncurtado);

            statement.executeUpdate();
        }
    }

    public static void exibirLinksNoBanco() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM links");
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Links no Banco de Dados:");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String original = resultSet.getString("original");
                String encurtado = resultSet.getString("encurtado");

                System.out.println("ID: " + id + ", Original: " + original + ", Encurtado: " + encurtado);
            }
        }
    }
}
