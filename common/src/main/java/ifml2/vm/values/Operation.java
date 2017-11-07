package ifml2.vm.values;

public enum Operation {
    ADD("сложение");

    final String caption;

    Operation(String caption) {
        this.caption = caption;
    }

    @Override
    public String toString() {
        return caption;
    }
}
