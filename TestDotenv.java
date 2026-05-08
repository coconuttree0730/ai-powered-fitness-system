import io.github.cdimascio.dotenv.Dotenv;

public class TestDotenv {
    public static void main(String[] args) {
        System.out.println("Working dir: " + System.getProperty("user.dir"));
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        System.out.println("REDIS_PASSWORD from .env: " + dotenv.get("REDIS_PASSWORD", "NOT_FOUND"));
        System.out.println("JWT_EXPIRATION from .env: " + dotenv.get("JWT_EXPIRATION", "NOT_FOUND"));
        System.out.println("All entries:");
        dotenv.entries().forEach(e -> System.out.println("  " + e.getKey() + "=" + e.getValue()));
    }
}