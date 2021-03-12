package nyanli.hackersmorph.other.mchorse.blockbuster.client.format;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import mchorse.blockbuster.api.formats.obj.OBJDataGroup;
import mchorse.blockbuster.api.formats.obj.OBJDataMesh;
import mchorse.blockbuster.api.formats.obj.OBJMaterial;
import mchorse.blockbuster.api.formats.obj.OBJParser;
import mchorse.blockbuster.api.formats.obj.Vector2f;
import mchorse.blockbuster.api.formats.obj.Vector3f;
import mchorse.blockbuster.commands.SubCommandBase;
import mchorse.mclib.utils.resources.RLUtils;

public class OBJParserFix extends OBJParser {
	
	private static final OBJMaterial unknown = new OBJMaterial("Unknown_Material");
	private static Constructor<?> OBJFace;
	
	static {
		try {
			Class<?> clazz = Class.forName("mchorse.blockbuster.api.formats.obj.OBJFace");
			OBJFace = clazz.getConstructor(String[].class);
			OBJFace.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			OBJFace = null;
		}
	}

	public static List<String> readAllLines(final InputStream stream) throws Exception {
		final List<String> list = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
			String line;
			while ((line = br.readLine()) != null) {
				list.add(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public OBJParserFix(InputStream objFile, InputStream mtlFile) {
		super(objFile, mtlFile);
	}

	@Override
	public void readMTL() throws Exception {
		if (this.mtlFile == null) {
			return;
		}
		final List<String> lines = readAllLines(this.mtlFile);
		OBJMaterial material = null;
		for (final String line : lines) {
			if (line.isEmpty()) {
				continue;
			}
			final String[] tokens = line.split("\\s+");
			final String first = tokens[0];
			if (first.equals("newmtl")) {
				material = new OBJMaterial(tokens[1]);
				this.materials.put(material.name, material);
			} else if (first.equals("Kd") && tokens.length == 4) {
				material.r = Float.parseFloat(tokens[1]);
				material.g = Float.parseFloat(tokens[2]);
				material.b = Float.parseFloat(tokens[3]);
			} else if (first.equals("map_Kd")) {
				material.useTexture = true;
			} else if (first.equals("map_Kd_linear")) {
				material.linear = true;
			} else {
				if (!first.equals("map_Kd_path")) {
					continue;
				}
				final String texture = String.join(" ", (CharSequence[]) SubCommandBase.dropFirstArgument(tokens));
				material.texture = RLUtils.create(texture);
			}
		}
	}

	@Override
	public void readOBJ() throws Exception {
		final List<String> lines = readAllLines(this.objFile);
		OBJDataMesh mesh = null;
		OBJMaterial material = unknown;
		for (final String line : lines) {
			final String[] tokens = line.split("\\s+");
			final String first = tokens[0];
			if ((first.equals("o") || first.equals("g")) && tokens.length >= 2) {
				final String name = tokens[1];
				mesh = null;
				for (final OBJDataMesh data : this.objects) {
					if (data.name.equals(name)) {
						mesh = data;
						break;
					}
				}
				if (mesh == null) {
					mesh = new OBJDataMesh();
					mesh.name = name;
					this.objects.add(mesh);
				}
			}
			if (first.equals("v")) {
				this.vertices.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
						Float.parseFloat(tokens[3])));
			} else if (first.equals("vt")) {
				this.textures.add(new Vector2f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2])));
			} else if (first.equals("vn")) {
				this.normals.add(new Vector3f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]),
						Float.parseFloat(tokens[3])));
			} else if (first.equals("usemtl")) {
				material = this.materials.get(tokens[1]);
			} else {
				if (!first.equals("f")) {
					continue;
				}
				OBJDataGroup group = null;
				for (OBJDataGroup g : mesh.groups) {
					if (g.material == material) {
						group = g;
						break;
					}
				}
				if (group == null) {
					group = new OBJDataGroup();
					group.material = material;
					mesh.groups.add(group);
				}
				final String[] faces = SubCommandBase.dropFirstArgument(tokens);
				List list = group.faces;
				if (faces.length == 4) {
					list.add(OBJFace.newInstance((Object) new String[]{faces[0], faces[1], faces[2]}));
					list.add(OBJFace.newInstance((Object) new String[]{faces[0], faces[2], faces[3]}));
				} else {
					list.add(OBJFace.newInstance((Object) faces));
				}
			}
		}
	}

}
