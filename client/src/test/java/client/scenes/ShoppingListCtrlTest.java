package client.scenes;

import commons.ShoppingItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingListCtrlTest {

    @Test
    void toMarkdown_emptyList_outputsEmptyMarker() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        String md = ctrl.toMarkdown();

        assertTrue(md.contains("# Shopping List"));
        assertTrue(md.contains("- (empty)"));
    }

    @Test
    void toMarkdown_preservesOrderAndFormatsAsBullets() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ctrl.addItemForTest(new ShoppingItem("Milk"));
        ctrl.addItemForTest(new ShoppingItem("Tomatoes"));
        ctrl.addItemForTest(new ShoppingItem("Bread"));

        String md = ctrl.toMarkdown();

        int milk = md.indexOf("- Milk");
        int tom = md.indexOf("- Tomatoes");
        int bread = md.indexOf("- Bread");

        assertTrue(milk >= 0);
        assertTrue(tom > milk);
        assertTrue(bread > tom);
    }

    @Test
    void toMarkdown_trimsAndSkipsBlankNames() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ctrl.addItemForTest(new ShoppingItem("  Milk  "));
        ctrl.addItemForTest(new ShoppingItem("   "));
        ctrl.addItemForTest(new ShoppingItem("Eggs"));

        String md = ctrl.toMarkdown();

        assertTrue(md.contains("- Milk"));
        assertTrue(md.contains("- Eggs"));
        assertFalse(md.contains("-    "));
    }
    @Test
    void addItems_addsAllItemsInOrder() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ctrl.addItems(List.of(
                new ShoppingItem("Milk"),
                new ShoppingItem("Tomatoes"),
                new ShoppingItem("Bread")
        ));

        String md = ctrl.toMarkdown();

        int milk = md.indexOf("- Milk");
        int tom = md.indexOf("- Tomatoes");
        int bread = md.indexOf("- Bread");

        assertTrue(milk >= 0);
        assertTrue(tom > milk);
        assertTrue(bread > tom);
    }

    @Test
    void addItems_allowsDuplicates() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ctrl.addItems(List.of(
                new ShoppingItem("Milk"),
                new ShoppingItem("Milk")
        ));

        String md = ctrl.toMarkdown();

        long milkCount = md.lines().filter(l -> l.trim().equals("- Milk")).count();
        assertEquals(2, milkCount);
    }

    @Test
    void addItems_ignoresNullOrEmptyInputSafely() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ctrl.addItems(null);
        ctrl.addItems(List.of());

        String md = ctrl.toMarkdown();
        assertTrue(md.contains("- (empty)"));
    }

    @Test
    void toMarkdown_doesNotCrashOnNullIngredientName() {
        ShoppingListCtrl ctrl = new ShoppingListCtrl();

        ShoppingItem bad = new ShoppingItem("Milk");
        bad.setIngredientName(null); // simulate corrupted/partial item

        ctrl.addItemForTest(bad);
        ctrl.addItemForTest(new ShoppingItem("Eggs"));

        String md = ctrl.toMarkdown();
        assertTrue(md.contains("- Eggs"));
    }
}
