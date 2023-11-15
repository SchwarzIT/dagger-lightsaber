package com.example;

import dagger.Component;

@Component(modules = {MyModule.class})
interface MyComponent {
    int myInt();
}
