import {TodoListPage} from "./todo-list.po";
import {browser, element, by} from 'protractor';

describe('angular-spark-lab-todos', () => {
    let page: TodoListPage;

    beforeEach(() => {
        page = new TodoListPage();
        page.navigateTo();
    });

it('should type something in the owner filter and check that it returned correct elements', () => {
        page.typeInput("ownerInput","Fry");

        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoOwner")).getText()).toEqual("Fry");
        })
    });

    it('should type something in the category filter and check that it returned correct elements', () => {
        page.typeInput("CategoryInput","homework");

        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoCategory")).getText()).toEqual("homework");
        })

    });

    it('should change the status filter to complete and check that it returned correct elements', () => {
        page.typeInput("statusInput","Complete", true);

        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoStatus")).getText()).toEqual("Complete");
        })

    });


    it('should type something in the search box and check that it returned correct element', () => {
        page.typeInput("searchInput","In sunt ex non tempor cillum");
        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoBody")).getText()).toContain("In sunt ex non tempor cillum");
        })
    });

    
    it('should change the status filter to incomplete and check that it returned correct elements', () => {
        page.typeInput("statusInput","Incomplete", true);

        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoStatus")).getText()).toEqual("Incomplete");
        })

    });

    it('should use multiple filters and check that it returned correct elements', () => {
        page.typeInput("CategoryInput","video games");
        page.typeInput("ownerInput","Roberta");
        page.typeInput("statusInput","Incomplete", true);

        page.getAllTodoCards().each(e => {
            expect(e.element(by.className("todoCategory")).getText()).toEqual("video games");
            expect(e.element(by.className("todoOwner")).getText()).toEqual("Roberta");
            expect(e.element(by.className("todoStatus")).getText()).toEqual("Incomplete");
        })

    });

   