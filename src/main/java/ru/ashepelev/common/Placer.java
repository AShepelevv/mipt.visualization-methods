package ru.ashepelev.common;

import ru.ashepelev.label.NoSolutionException;

public interface Placer {
     void place() throws NoSolutionException;
}
