# CSEP Template Project

This repository contains the template for the CSE project. Please extend this README.md with sufficient instructions that will illustrate for your TA and the course staff how they can run your project.

To run the template project from the command line, you either need to have [Maven](https://maven.apache.org/install.html) installed on your local system (`mvn`) or you need to use the Maven wrapper (`mvnw`). You can then execute

	mvn -pl server -am spring-boot:run

to run the server and

	mvn -pl client -am javafx:run

to run the client. Please note that the server needs to be running, before you can start the client.

4.1 Basic requirements:
Ingredients: For details of adding ingredients, please check 4.3. To edit and delete an ingredient, select the ingredient, then click the corresponding button. 
Steps:To add a new step press the “+Add step” button. To edit a step press the “Edit” button found next to the step text. To delete a recipe press the red “X”, which is next to the “Edit button”
Clone: Press the clone button and a new clone recipe will appear in the list view of the form: Recipe(n), n being the number of the copy(so if you copy once you get Recipe(1), if you copy again you get Recipe(2), etc.) The copy is not automatically selected so the user should select it from the list view if they want to edit it.
Download: Click the “Download” button, a markdown file will be downloaded. 

4.2 Automated Change Synchronisation
When 2 or more clients are open, and changes are made in the recipes of one of the clients (name, recipe ingredients, recipe steps), the changes are immediately reflected in the other clients. When a change is made in one client, the server broadcasts the change to the rest of the clients so all are updated instantly.

4.3 Nutritional Value 
To view and manage nutritional values, click the “Ingredients” button at the top of the interface to open the ingredient overview.
Click “Add Ingredient” to create a new ingredient. The name, protein, fat, and carbohydrates values must be entered. Select an ingredient and click “Edit nutrition” to edit the name or nutritional values. Click “Delete Ingredient” to delete an ingredient. If the ingredient is used in one or more recipes, a warning dialog will be shown. 
Click “Recipes” at the top to go back to the Recipe interface. In the Recipe interface, a dropdown list is provided when adding ingredients. Clicking “Add” allows you to choose from all existing ingredients. If you want to add a new ingredient, click “Add new ingredient”. The new ingredient will be displayed in the ingredient interface automatically. Each recipe has a default serving size of 1. Click “Edit servings” to change the number of servings. Clicking “Scale” scales the recipe according to the serving size, and clicking “Reset scale” restores the recipe to size 1. The nutritional values and serving sizes are visible right below the recipe handling buttons and updated when the recipe is scaled. 
All required functionalities have been implemented, except for the informal amount feature.

4.4 Searching for a recipe:	The star next to each recipe name indicates whether the recipe is favourite or not, if the star is grey the recipe is not favourite and if it is clicked it turns yellow meaning it is now favourite. The two buttons at the just below the search bar indicate what appears in the list view. The “All Recipes” button means that the list view contains all the recipes, favourite or not and the “Favourite Recipes” turns the list to show only the favourite recipes.

4.5 Shopping List
To have a view of required ingredients to buy, we have a shopping list that can be seen by clicking the Shopping List button on the top row. The shopping list stores ingredients with the quantity needed to buy. 
Ingredients can be added: 
Directly to the shopping list by clicking the dropdown menu next to ingredient and selecting the desired ingredient, then after clicking the ‘Add’ button, entering the amount and unit adds the ingredient with the quantity. 
From a recipe by clicking the ‘Shop’ button next to the recipe ingredients. This takes the user to an overview page, where they can see the ingredients of the recipe with the amounts in the recipe. The amounts can be modified by double clicking the value in the amount column and entering the desired amount.
In this overview page, ingredients can be added by clicking the dropdown menu and selecting the ingredient, then clicking the add button and entering the amount and unit. Selecting an ingredient row in the table and clicking the ‘Remove selected’ button removes the ingredient from the overview page. 
Then we can click the ‘Add to shopping list’ button to confirm adding all these ingredients to the shopping list.
If the same ingredient is added from different recipes, it appears multiple times in the shopping list, and the source recipe is mentioned in parentheses.
The shopping list has a reset button, if the user wants to erase all the items in the shopping list.
The shopping list also has a download button, which lets you download a printable version of the list in the appropriate format. 
Both buttons are displayed on the right of the shopping list page.

##Use of Generative AI
For codes generated by AI, a comment is placed directly above the relevant method
describing how the tool was used. 

