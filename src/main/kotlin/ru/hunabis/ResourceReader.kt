import java.nio.file.Files;
import java.nio.file.Paths;

object ResourceReader {

	fun getAllFilesInResources(dir: String): List<String> {
		var list = mutableListOf<String>();
		var aPath = "/src/main/resources" + dir;
		val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString();
		val resourcesPath = Paths.get(projectDirAbsolutePath, aPath);

		Files.walk(resourcesPath).filter {
			item -> Files.isRegularFile(item);
		}.forEach {
			item -> list.add(item.toString().split("$aPath/").last());
        }
        return list;
	}

	fun readTextResource(path: String): String = try { object {}.javaClass.getResource(path).readText() } catch(e: Exception) { "" }
}
