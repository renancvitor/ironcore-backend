package com.ironcore.application.shared.pagination;

public record PageQuery(
        int page,
        int size
) {

    private static final int MAX_PAGE_SIZE = 100;

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Página não pode ser negativa.");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Tamanho da página deve estar entre 1 e " + MAX_PAGE_SIZE + ".");
        }
    }
}
