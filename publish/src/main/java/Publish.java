import beans.Artifact;
import beans.Bootstrap;
import beans.GitHubAsset;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class Publish
{
    private static final String BLUELITE_UPDATE_TOKEN = "BLUELITE_UPDATE_TOKEN";
    private static final String RELEASE_ID = "23915541";
    private static final String USER = "Bloodspawns";
    private static final String REPO = "c0603cb96187d5c295173c5c90d3b389671964dab55056f913c3d86c3333300b";
    private static final String BOOTSTRAP_PATH = "build/libs/bootstrap.json";
    private static final String BLUELITE_ASSET_UPDATE_URL = "https://uploads.github.com/repos/" + USER + "/" + REPO + "/releases/" + RELEASE_ID + "/assets";
    private static final String BLUELITE_ASSETS_URL = "https://api.github.com/repos/" + USER + "/" + REPO + "/releases/" + RELEASE_ID + "/assets";
    private static final String BLUELITE_BOOTSTRAP_URL = "https://github.com/" + USER + "/" + REPO + "/releases/download/1.0/bootstrap.json";
    private static final String BLUELITE_ASSET_DOWNLOAD_URL = "https://github.com/" + USER + "/" + REPO + "/releases/download/1.0/";
    private File targetDependency;

    public Publish(File targetDependency)
    {
        this.targetDependency = targetDependency;
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length < 1)
        {
            System.err.println("input: url dependency");
        }
        new Publish(new File(args[0])).publish();
    }

    private static boolean isTargetDependency(String name)
    {
        return name.matches("^bluelite-api.jar$$");
    }

    private static boolean isBootstrapDep(String name)
    {
        return name.matches("^bootstrap.json$");
    }

    private static String hash(File file) throws IOException
    {
        HashFunction sha256 = Hashing.sha256();
        return Files.asByteSource(file).hash(sha256).toString();
    }

    public void publish() throws IOException
    {
        Bootstrap blueStrap = getBlueBootstrap();
        Artifact targetDependency = null;

        ArrayList<Artifact> artifacts = new ArrayList<>(Arrays.asList(blueStrap.getArtifacts()));
        for (Artifact artifact : artifacts)
        {
            if (isTargetDependency(artifact.getName()))
            {
                targetDependency = artifact;
                break;
            }
        }
        if (targetDependency == null)
        {
            targetDependency = new Artifact();
            targetDependency.setPath(BLUELITE_ASSET_DOWNLOAD_URL + this.targetDependency.getName());
            artifacts.add(targetDependency);
        }

        targetDependency.setSize((int) this.targetDependency.length());
        targetDependency.setName(this.targetDependency.getName());
        String hash = hash(this.targetDependency);
        targetDependency.setHash(hash);

        blueStrap.setArtifacts(artifacts.toArray(new Artifact[0]));
        Gson g = new GsonBuilder().setPrettyPrinting().create();
        String json = g.toJson(blueStrap);

        try (PrintWriter out = new PrintWriter(BOOTSTRAP_PATH)) {
            out.println(json);
        }

        upload(new File(BOOTSTRAP_PATH), this.targetDependency);
    }

    private void upload(File bootstrap, File pluginsDependency) throws IOException
    {
        Map<String, String> env = System.getenv();
        if (!env.containsKey(BLUELITE_UPDATE_TOKEN))
        {
            throw new RuntimeException("Missing github token");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(BLUELITE_ASSETS_URL).openConnection();
        connection.setUseCaches(false);
        connection.setRequestProperty("Authorization", "token " + env.get(BLUELITE_UPDATE_TOKEN));
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            result.append(line);
        }
        Gson g = new Gson();
        GitHubAsset[] assets = g.fromJson(result.toString(), GitHubAsset[].class);

        for (GitHubAsset asset : assets)
        {
            if (isTargetDependency(asset.getName()))
            {
                if (!requestDelete(asset))
                {
                    System.err.println("Couldn't delete " + asset.getName() + ":: " + asset.getUrl());
                }
                if (!requestUpload(asset, pluginsDependency))
                {
                    System.err.println("Couldn't upload " + asset.getName());
                }
            }
            else if (isBootstrapDep(asset.getName()))
            {
                if (!requestDelete(asset))
                {
                    System.err.println("Couldn't delete " + asset.getName() + ":: " + asset.getUrl());
                }
                if (!requestUpload(asset, bootstrap))
                {
                    System.err.println("Couldn't upload " + asset.getName());
                }
            }
        }
    }

    private boolean requestUpload(GitHubAsset asset, File file) throws IOException
    {
        Map<String, String> env = System.getenv();
        if (!env.containsKey(BLUELITE_UPDATE_TOKEN))
        {
            throw new RuntimeException("Missing github token");
        }

        String param1 = asset.getName();
        String query = String.format("name=%s",
                URLEncoder.encode(param1, Charsets.UTF_8.name()));

        HttpURLConnection connection = (HttpURLConnection) new URL(BLUELITE_ASSET_UPDATE_URL + "?" + query).openConnection();

        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "token " + env.get(BLUELITE_UPDATE_TOKEN));
        connection.setRequestProperty("Content-Type", "application/zip");
        connection.setRequestMethod("POST");

        DataOutputStream request = new DataOutputStream(connection.getOutputStream());
        FileInputStream fis = new FileInputStream(file);
        ByteStreams.copy(fis, request);
        request.close();
        fis.close();

        return connection.getResponseCode() == 201;
    }

    private boolean requestDelete(GitHubAsset asset) throws IOException
    {
        Map<String, String> env = System.getenv();
        if (!env.containsKey(BLUELITE_UPDATE_TOKEN))
        {
            throw new RuntimeException("Missing github token");
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(asset.getUrl()).openConnection();
        connection.setRequestProperty("Authorization", "token " + env.get(BLUELITE_UPDATE_TOKEN));
        connection.setRequestMethod("DELETE");
        connection.setUseCaches(false);
        return connection.getResponseCode() == 204;
    }

    private static Bootstrap getBlueBootstrap() throws IOException
    {
        URL u = new URL(BLUELITE_BOOTSTRAP_URL);

        URLConnection conn = u.openConnection();
        conn.setUseCaches(false);

        try (InputStream i = conn.getInputStream())
        {
            byte[] bytes = ByteStreams.toByteArray(i);

            Gson g = new Gson();
            return g.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes)), Bootstrap.class);
        }
    }
}
