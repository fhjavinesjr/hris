-- Seed only the known existing master-data meanings; never replace an officer's mapping.
UPDATE natureofseparation SET birSeparationReasonCode = 'T'
WHERE code = 'RES' AND UPPER(nature) = 'RESIGNATION'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

UPDATE natureofseparation SET birSeparationReasonCode = 'R'
WHERE code = 'RET' AND UPPER(nature) = 'RETIREMENT (COMPULSORY)'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

UPDATE natureofseparation SET birSeparationReasonCode = 'R'
WHERE code = 'RETT' AND UPPER(nature) = 'RETIREMENT'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

UPDATE natureofseparation SET birSeparationReasonCode = 'D'
WHERE code = 'DEC' AND UPPER(nature) = 'DECEASED'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

-- These two are T only when the nature represents actual end of employment,
-- not a transfer to another employing agency. Officers may change them to TR.
UPDATE natureofseparation SET birSeparationReasonCode = 'T'
WHERE code = 'ROL' AND UPPER(nature) = 'ROLL OFF'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

UPDATE natureofseparation SET birSeparationReasonCode = 'T'
WHERE code = 'DIS' AND UPPER(nature) = 'DISMISSAL/REMOVAL'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');

UPDATE natureofseparation SET birSeparationReasonCode = 'T'
WHERE code = 'SEPREOG' AND UPPER(nature) = 'SEPARATION DUE TO REORGANIZATION'
  AND (birSeparationReasonCode IS NULL OR birSeparationReasonCode = '');
