import {Component, OnInit} from '@angular/core';
import {TodoListService} from "./todo-list.service";
import {Todo} from "./todo";

@Component({
    selector: 'todo-component',
    styleUrls: ['./todo.component.css'],
    templateUrl: 'todo.component.html'
})
export class TodoComponent implements OnInit {
    public todo: Todo = null;
    private id: string;

    constructor(private todoListService: TodoListService) {
        // this.users = this.userListService.getUsers();
    }

    private subscribeToServiceForId() {
        if (this.id) {
            this.todoListService.getTodoById(this.id).subscribe(
                todo => this.todo = todo,
                err => {
                    console.log(err);
                }
            );
        }
    }

    setId(id: string) {
        this.id = id;
        this.subscribeToServiceForId();
    }

    ngOnInit(): void {
        this.subscribeToServiceForId();
    }
}
