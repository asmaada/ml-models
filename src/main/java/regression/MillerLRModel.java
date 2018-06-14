/**
 * Created by Lauren on 6/5/18.
 */

package regression;
import org.apache.commons.math3.stat.regression.MillerUpdatingRegression;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.math3.stat.regression.RegressionResults;

public class MillerLRModel extends LRModel{
    private MillerUpdatingRegression R;
    private RegressionResults trained;
    private int numVars;

    MillerLRModel(String name, boolean constant, int numVars) {
        super(name, Framework.Miller);
        R = new MillerUpdatingRegression(numVars, constant);
        this.numVars = numVars;
    }

    @Override
    protected long getN() {
        return R.getN();
    }

    @Override
    long getNumVars() { return numVars; }

    @Override
    boolean hasConstant() {return R.hasIntercept();}

    @Override
    void add(List<Double> given, double expected) {
        if (given.size() != numVars) throw new IllegalArgumentException("Incorrect number of variables in given.");
        double[] givenArr = LR.convertFromList(given);
        R.addObservation(givenArr, expected);
        state = State.training;
    }

    @Override
    LR.ModelResult train() {
        trained = R.regress();
        double[] parameters = trained.getParameterEstimates();
        state = State.ready;
        return new LR.ModelResult(name, framework, hasConstant(), getNumVars(), state, getN()).withInfo("parameters", LR.doubleArrayToList(parameters));
    }

    @Override
    double predict(List<Double> given) {
        if (state == State.created || state == State.training) train();
        if (state == State.ready) {
            double[] params = trained.getParameterEstimates();
            double result;
            if (R.hasIntercept()) {
                result = params[0];
                for (int i = 0; i < numVars; i++) result += params[i+1]*given.get(i);
            } else {
                result = 0;
                for (int i = 0; i < numVars; i++) result += params[i]*given.get(i);
            }
            return result;
        }
        throw new RuntimeException("Model in state '" + state.name() + "' so cannot make predictions.");
    }

    @Override
    Object data() {
        return trained.getParameterEstimates();
    }

    @Override
    LR.ModelResult asResult() {
        LR.ModelResult r = new LR.ModelResult(name, framework, hasConstant(), getNumVars(), state, getN());
        return trained != null ? r.withInfo("parameters", LR.doubleArrayToList(trained.getParameterEstimates()), "SSE", trained.getErrorSumSquares(),
                "R squared", trained.getRSquared(), "adjusted R squared", trained.getAdjustedRSquared(), "MSE", trained.getMeanSquareError(),
                "regression sum squares", trained.getRegressionSumSquares(), "parameters std error", trained.getStdErrorOfEstimates(),
                "total sum squares", trained.getTotalSumSquares()): r;
    }
}
