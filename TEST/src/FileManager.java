import java.io.*;
import java.util.*;
public class FileManager {
	

	public List<Dishes> parseFile(File file) {
		String str = null;
		List<Dishes> list = new ArrayList<Dishes>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((str = br.readLine()) != null) {
				if (str.startsWith("Name:")) {
					int index = str.indexOf(":");
					String dishName = str.substring(index + 1, str.length());
					
					str = br.readLine();
					index = str.indexOf(":");
					String categoriesString = str.substring(index + 1, str.length());
					
					String[] cateArr = categoriesString.split(",");
					Set<String> categories = new HashSet<String>();
					for (int j = 0; j < cateArr.length; j++) {
						categories.add(cateArr[j]);
					}
					
					Set<String> ingredients = new HashSet<String>();
					String ss = br.readLine();
					
					while((str = br.readLine()) != null && !(str.length() == 0)) {
						String[] strArr = str.split(" ");
						for (int i = 0; i < strArr.length; i++) {
							ingredients.add(strArr[i]);
						}
					}
					Dishes d = new Dishes();
					d.setName(dishName);
					d.setCategories(categories);
					d.setIngredients(ingredients);
					
					list.add(d);
					
				}
			}
		} 
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}
	public static void main(String[] args) {
		// TODO auto-generated method stub
		FileManager fm = new FileManager();
		List<Dishes> list = new ArrayList<Dishes>();
		File file = new File("dishes.txt");
		list = fm.parseFile(file);
		
		for (int i = 0; i < list.size(); i++ ) {
			System.out.println(list.get(i).getName());
			System.out.println(list.get(i).getCategories());;
			System.out.println(list.get(i).getIngredients());
		}
	}
}

class Dishes {
	private String name;
	private Set<String> Categories;
	private Set<String> ingredients;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Set<String> getCategories() {
		return Categories;
	}
	
	public void setCategories(Set<String> categories) {
		Categories = categories;
	}
	
	public Set<String> getIngredients(){
		return ingredients;
	}
	
	public void setIngredients(Set<String> ingredients) {
		this.ingredients = ingredients;
	}
}



