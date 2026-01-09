package client.scenes;

import commons.ShoppingItem;
import org.junit.jupiter.api.Test;

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

        // add items without JavaFX by using a tiny helper method below (see note)
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
}
