package marumasa.marumasa_sign.client;

import marumasa.marumasa_sign.MarumaSign;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CustomSignParameter {

    // 画像 読み込み済みリスト
    private static final List<Identifier> LoadedList = new ArrayList<>();
    // テクスチャマネージャー
    private static final TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

    public final String StringURL;
    public final Translation translation;
    public final Scale scale;
    public final Quaternionf rotation;

    public final RenderLayer renderLayer;

    public CustomSignParameter(
            // 画像のURL
            String StringURL,
            // 位置
            float TranslationX,
            float TranslationY,
            float TranslationZ,
            // 大きさ
            float ScaleX,
            float ScaleY,
            float ScaleZ,
            // 回転
            float RotationX,
            float RotationY,
            float RotationZ
    ) {

        this.StringURL = StringURL;

        Identifier identifier = new Identifier(MarumaSign.MOD_ID, URLtoID());

        // もし URL の画像が まだ読み込んだことがなかったら (読み込み済みリストに なかったら)
        if (!LoadedList.contains(identifier)) {
            try {
                AbstractTexture texture = new NativeImageBackedTexture(
                        NativeImage.read(new URL(this.StringURL).openStream())
                );

                // テクスチャ 登録
                textureManager.registerTexture(identifier, texture);

                // 読み込み済みリストに 追加
                LoadedList.add(identifier);
                // ログ出力
                MarumaSign.LOGGER.info("Load: " + this.StringURL + " : " + identifier);
            } catch (IOException e) {
                // URL から 画像を読み込めなかったら
                MarumaSign.LOGGER.warn("Failure" + this.StringURL + " : " + identifier);
            }
        }

        // getText で 透過と半透明 対応の RenderLayer 生成
        // RenderLayer.getEntityTranslucent() では プレイヤーの向いている角度によって明度が変わってしまう
        // 確認したバージョン 1.20.1
        this.renderLayer = RenderLayer.getText(identifier);

        // 位置を設定
        this.translation = new Translation(TranslationX, TranslationY, TranslationZ);

        // 大きさを設定
        this.scale = new Scale(ScaleX, ScaleY, ScaleZ);

        // X 軸の 回転を設定するための クォータニオン 作成
        Quaternionf qx = new Quaternionf();
        // X 軸に どれくらい回転するか 設定
        qx.fromAxisAngleDeg(1, 0, 0, RotationX);

        // Y 軸の 回転を設定するための クォータニオン 作成
        Quaternionf qy = new Quaternionf();
        // Y 軸に どれくらい回転するか 設定
        qy.fromAxisAngleDeg(0, 1, 0, RotationY);

        // Z 軸の 回転を設定するための クォータニオン 作成
        Quaternionf qz = new Quaternionf();
        // Z 軸に どれくらい回転するか 設定
        qz.fromAxisAngleDeg(0, 0, 1, RotationZ);

        // クォータニオンの積 計算 を 計算して 回転を設定
        this.rotation = new Quaternionf().mul(qx).mul(qy).mul(qz);
    }

    public static CustomSignParameter load(SignBlockEntity sign) {
        return new CustomSignParameter(
                "http://localhost/test.png",
                0,
                0,
                0,
                16,
                9,
                1,
                90f,
                0,
                0
        );
        // return null;
    }

    // URLを Identifier で使える ID に変換
    private String URLtoID() {
        // 文字列をバイト配列に変換
        byte[] bytes = this.StringURL.getBytes();
        // バイナリ文字列を格納する変数
        StringBuilder binary = new StringBuilder();
        // バイト配列の要素ごとにバイナリ文字列に変換
        for (byte b : bytes) binary.append(Integer.toBinaryString(b));
        // 0をaに置き換え
        binary = new StringBuilder(binary.toString().replace('0', 'a'));
        // 1をbに置き換え
        binary = new StringBuilder(binary.toString().replace('1', 'b'));
        return binary.toString();
    }

    public record Translation(float x, float y, float z) {

        public Translation(float x, float y, float z) {
            // ブロックの中心に移動する
            this.x = x + 0.5f;
            this.y = y + 0.5f;
            this.z = z + 0.5f;
        }
    }

    // 画像の大きさを
    public record Scale(float x, float y, float z) {
        public Scale(float x, float y, float z) {
            // ブロックの半分の大きさにする
            this.x = x * 0.5f;
            this.y = y * 0.5f;
            this.z = z * 0.5f;
        }
    }
}
