{
    "solver": "2d",
    "calculationConstants": {
        "CFL": 0.5,
        "xRes": 500,
        "yRes": 500
    },
    "physicalConstants": {
        "gamma": 1.6666666666,
        "xLength": 1.0,
        "yLength": 1.0,
        "totalTime": 0.1
    },
    "restorator": {
        "type": "eta",
	"eta" : 0.333333333
    },
    "conditions_input_type": "steady_shock_wave",
    "SSW_conditions_data": {
        "type": "hydro",
        "x_s": 0.5,
        "angle": 45,
        "k_y": 3,
        "uAmpRel": 0.0,
        "rhoAmpRel": 0.025,
        "leftValues": {
	  "rho" : 0.2, "u" : 20.0, "v" : 0.0, "w" : 0.0, "p" : 0.4,
	  "bX" : 0.0, "bY" : 0.0, "bZ" : 0.0
	}
    }
}
