package one.jpro.auth.oath2;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Options describing Key stored in PEM format.
 *
 * @author Besmir Beqiri
 */
public class PubSecKeyOptions {

    private String algorithm;
    private Buffer buffer;
    private String id;

//    private boolean certificate;
//    private Boolean symmetric;
//    private String publicKey;
//    private String secretKey;

    /**
     * Default constructor
     */
    public PubSecKeyOptions() {
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public PubSecKeyOptions algorithm(String algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    /**
     * The PEM or Secret key buffer. When working with secret materials, the material is expected to be encoded in
     * {@code UTF-8}. PEM files are expected to be {@code US_ASCII} as the format uses a base64 encoding for the
     * payload.
     *
     * @return the buffer.
     */
    public Buffer getBuffer() {
        return buffer;
    }

    /**
     * The PEM or Secret key buffer. When working with secret materials, the material is expected to be encoded in
     * {@code UTF-8}. PEM files are expected to be {@code US_ASCII} as the format uses a base64 encoding for the
     * payload.
     *
     * @return self.
     */
    public PubSecKeyOptions buffer(String buffer) {
        this.buffer = ByteBuffer.wrap(buffer.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    /**
     * The PEM or Secret key buffer. When working with secret materials, the material is expected to be encoded in
     * {@code UTF-8}. PEM files are expected to be {@code US_ASCII} as the format uses a base64 encoding for the
     * payload.
     *
     * @return self.
     */
    public PubSecKeyOptions buffer(Buffer buffer) {
        this.buffer = buffer;
        return this;
    }

    public String getId() {
        return id;
    }

    public PubSecKeyOptions id(String id) {
        this.id = id;
        return this;
    }
}
