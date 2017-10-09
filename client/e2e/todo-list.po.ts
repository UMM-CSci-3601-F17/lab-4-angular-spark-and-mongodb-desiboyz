import {browser, element, by} from 'protractor';
import {Key} from "selenium-webdriver";

export class TodoPage {
    navigateTo() {
        return browser.get('/todos');
    }

    //http://www.assertselenium.com/protractor/highlight-elements-during-your-protractor-test-run/
    highlightElement(byObject) {
        function setStyle(element, style) {
            const previous = element.getAttribute('style');
            element.setAttribute('style', style);
            setTimeout(() => {
                element.setAttribute('style', previous);
            }, 200);
            return "highlighted";
        }

        return browser.executeScript(setStyle, element(byObject).getWebElement(), 'color: red; background-color: yellow;');
    }

    getTodoTitle() {
        let title = element(by.id('list-title')).getText();
        this.highlightElement(by.id('list-title'));

        return title;
    }

    typeAnOwner(name: string) {
        let input = element(by.id('todoOwner'));
        input.click();
        input.sendKeys(name);

    }

    grabACategory(category: string) {
        let input = element(by.id('categories'));
        input.click();
        input.sendKeys(category);
        this.pressEnter();
        this.toggleSearch();

    }

    selectStatus(status: string) {
        let input = element(by.id('status'));
        input.click();
        input.sendKeys(status);
        this.pressEnter();
    }


    filterByContent(content: string) {
        let input = element(by.id('content-search'));
        input.click();
        input.sendKeys(content);
    }

    addTodo(owner:string,status:string,body:string,category:string) {
        element(by.id("add-owner")).click();
        element(by.id("add-owner")).sendKeys(owner);
        element(by.id(status)).click();
        element(by.id("content")).click();
        bodyField.sendKeys(body);
        element(by.id("category")).click();
        element(by.id("category")).sendKeys(category);
        element(by.id("todo")).click();
    }
    countTodosOnScreen(){
        let countTodos = element(by.id("todos"));

    }

}