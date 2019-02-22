package io.prover.swypeid.enterprise.viewholder.model;


import io.prover.swypeid.enterprise.R;

public enum Hints {
    Other, AllDone, MakeCircular, SwypeCodeFailed, NoMoney, VideoNotConfirmed, CalculatingVideoHash, VideoHashPosted, NetworkError,
    ErrorPostingHash, FileHashAddedToBlockchain, LowContrast;

    public Hint object() {

        switch (this) {
            case AllDone:
                return new Hint.Builder(this)
                        .setMessage(R.string.swypeCodeOk, 3500)
                        .setImageId(R.drawable.ic_all_done, 2000)
                        .setMinifyDrawableId(R.drawable.ic_all_done_shrink)
                        .setStartDelay(1200)
                        .build();

            case MakeCircular:
                return new Hint.Builder(this)
                        .setMessage(R.string.makeProver, 5000)
                        .setImageId(R.drawable.make_circular_movement_animated, 2000)
                        .setMinifyDrawableId(R.drawable.make_circular_movement_minimize)
                        .setMinifiedDrawableId(R.drawable.make_circular_movement_minimized)
                        .setMinifiedDelay(15_000)
                        .setMinifiedRepeatCount(-1)
                        .build();

            case VideoNotConfirmed:
                return new Hint.Builder(this)
                        .setMessage(R.string.videoNotConfirmed, 4000)
                        .setImageId(R.drawable.ic_not_verified_anim, 3000)
                        .build();

            case SwypeCodeFailed:
                return new Hint.Builder(this)
                        .setMessage(R.string.swypeCodeFailedTryAgain, 3500)
                        .setImageId(R.drawable.make_circular_movement_animated, 2000)
                        .setMinifyDrawableId(R.drawable.make_circular_movement_minimize)
                        .setMinifiedDrawableId(R.drawable.make_circular_movement_minimized)
                        .setMinifiedDelay(1500)
                        .setMinifiedRepeatCount(-1)
                        .setStartDelay(1000)
                        .build();

            case NoMoney:
                return new Hint.Builder(this)
                        .setMessage(R.string.notEnoughMoney, 3500)
                        .setImageId(R.drawable.no_money_anim, 3000)
                        .build();

            case VideoHashPosted:
                return new Hint.Builder(this)
                        .setMessage(R.string.videoHashPosted, 3000)
                        .build();

            case CalculatingVideoHash:
                return new Hint.Builder(this)
                        .setMessage(R.string.calculatingVideoHash, 4000)
                        .build();

            case ErrorPostingHash:
                return new Hint.Builder(this)
                        .setMessage(R.string.videoHashPostFailed, 4000)
                        .build();

            case FileHashAddedToBlockchain:
                return new Hint.Builder(this)
                        .setMessage(R.string.videoHashAddedToBlockchain, 3000)
                        .build();

            case LowContrast:
                return new Hint.Builder(this)
                        .setMessage(R.string.lowContrastWarning, Integer.MAX_VALUE)
                        .setUseTopHintView(false)
                        .setWarning(true)
                        .setAnimationTime(100)
                        .setMinHintVisibleTime(0)
                        .build();

            default:
                throw new RuntimeException("Not implemented for: " + this);
        }
    }
}
