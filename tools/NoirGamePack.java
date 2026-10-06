import com.noir.game.engine.encryption.NoirGameCrypto;
import java.nio.file.*;

/**
 * Command-line packer for Noir encrypted .game files. The key is read from NOIR_GAME_KEY;
 * it is never accepted as a command-line argument, preventing accidental shell-history leaks.
 */
public final class NoirGamePack {
    public static void main(String[] args) throws Exception {
        if(args.length != 3 || !args[0].equals("encrypt")) {
            System.err.println("Usage: java NoirGamePack encrypt input.game output.game");
            System.exit(2);
        }
        String key=System.getenv("NOIR_GAME_KEY");
        if(key==null || key.length()<8) throw new IllegalStateException("Set NOIR_GAME_KEY (8+ characters) in the environment");
        byte[] source=Files.readAllBytes(Path.of(args[1]));
        byte[] packed=NoirGameCrypto.encrypt(source,key.toCharArray());
        Files.write(Path.of(args[2]),packed);
    }
}
