
import {Injectable} from '@angular/core';
import {Http} from '@angular/http';

import {Observable} from "rxjs";
import "rxjs/add/operator/map";

import {Todo} from './todo';
import {environment} from "../../environments/environment";

@Injectable()
export class TodoListService {
    private todoUrl: string = environment.API_URL + "todos";

    constructor(private http: Http) {
    }

    getTodos(): Observable<Todo[]> {
        let observable: Observable<any> = this.http.request(this.todoUrl);
        return observable.map(res => res.json());
    }

    getTodoById(id: string): Observable<Todo> {
        return this.http.request(this.todoUrl + "/" + id).map(res => res.json());
    }

    addNewTodo(owner: string, body : string, category : string): Observable<Boolean> {
        const body1 = {owner:owner, body:body, category:category};
        console.log(body1);

        //Send post request to add a new todo with the todo data as the body with the specified headers.
        return this.http.post(this.todoUrl + "/new", body1).map(res => res.json());
    }
}
