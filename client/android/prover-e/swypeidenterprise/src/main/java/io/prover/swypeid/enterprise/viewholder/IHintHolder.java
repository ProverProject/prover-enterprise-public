package io.prover.swypeid.enterprise.viewholder;

public interface IHintHolder {

    void cancel();

    void setShowShort();

    void show();

    void hide();

    HintState getState();

    enum HintState {
        ExpectingToShow, Visible, Minifying, Minified, Hiding, Hidden;

        boolean isHidingOrHidden() {
            return this == Hidden || this == Hiding;
        }
    }

    interface HintListener {
        void onHintHidden(IHintHolder hintHolder);

        void OnHintMinified(IHintHolder hintHolder);
    }
}
